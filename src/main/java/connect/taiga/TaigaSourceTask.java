package connect.taiga;

import connect.github.GithubSchema;
import connect.taiga.TaigaApi;
import connect.taiga.TaigaSchema;
import model.taiga.*;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class TaigaSourceTask extends SourceTask{

    private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    static {
        dfZULU.setTimeZone(tzUTC);
    }

    private String version = "0.0.1";

    private String taigaUrl;
    private String taigaSlug;
    private String taigaUser;
    private String taigaPass;
    private String taigaProjectId;
    private String issuetopic;
    private String metrictopic;
    private String taigaInterval;
    private Integer interval;

    private String createdSince;
    private String updatedSince;

    private Date mostRecentUpdate;

    private static DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");

    // millis of last poll
    private long lastPoll = 0;

    private Boolean firstPoll = true;

    private Logger log = Logger.getLogger(TaigaSourceTask.class.getName());

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        List<SourceRecord> records = new ArrayList<>();

        log.info("lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval );

        if ( lastPoll != 0 ) {
            if ( System.currentTimeMillis() < ( lastPoll + (interval * 1000) ) ) {
                log.info("------- exit polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
                Thread.sleep(1000);
                return records;
            }
        }

        lastPoll = System.currentTimeMillis();

        int offset = 1;
        Issue[] redmineIssues;
        Project myProject;
        // if mostRecentUpdate is available from offset -> storage use it

        if(firstPoll == true){
            if ( mostRecentUpdate != null ) {
                updatedSince = onlyDate.format(mostRecentUpdate);
            } else {
                updatedSince = "2000-01-01";
                try{
                    mostRecentUpdate=onlyDate.parse(updatedSince);
                }catch(ParseException e){
                    log.info("unable to parse "+updatedSince);
                    throw new InterruptedException();
                }
            }
        }else{
            log.info("Query updated since: " + mostRecentUpdate);
        }

        int total_issues= 0;
        Date maxUpdatedOn = null;
        do {

            myProject = TaigaApi.getProject(taigaUrl, taigaProjectId, taigaUser, taigaPass);
            redmineIssues = TaigaApi.getIssuesByProjectId(taigaUrl, taigaProjectId, taigaUser, taigaPass);
            //MIRI TOTS ELS MODIFIED DATE
            if (maxUpdatedOn == null &&  redmineIssues.length > 0 ) {
                maxUpdatedOn=redmineIssues[0].modified_date;
            }

            total_issues += redmineIssues.length;
            // download up to that moment
            //if(redmineIssues.length == 0 || mostRecentUpdate.compareTo(redmineIssues[0].modified_date) >= 0)break;
            records.addAll( getIssueSourceRecords(redmineIssues, mostRecentUpdate, myProject) );
            offset += 1;
            break;
        } while (true);
        Epic[] myEpics;
        UserStory[] myUserStories;
        Task[] myTasks;
        Milestone[] myMilestones;

        do {
            myEpics = TaigaApi.getEpicsByProjectID(taigaUrl, taigaProjectId, taigaUser, taigaPass);
            myUserStories = TaigaApi.getUserStroriesByProjectId(taigaUrl, taigaProjectId, taigaUser, taigaPass);
            myTasks = TaigaApi.getTasks(taigaUrl, taigaProjectId, taigaUser, taigaPass);
            myMilestones = TaigaApi.getMilestonesByProjectId(taigaUrl, taigaProjectId, taigaUser, taigaPass);

            records.addAll( getTaigaMetrics(myEpics, myUserStories, myTasks, myMilestones));
            break;
        } while (true);

        mostRecentUpdate = maxUpdatedOn;
        firstPoll = false;

        return records;
    }

    private List<SourceRecord> getTaigaMetrics(Epic[] epics, UserStory[] us, Task[] tasks, Milestone[] milestones) {

        List<SourceRecord> result = new ArrayList<>();

        for ( Epic e : epics) {

            log.info("EPIC ID: " + e.id);
            log.info("EPIC URL: " + taigaUrl+"/epics/"+e.id);

            Struct struct = new Struct( TaigaSchema.taigaEpic);
            struct.put( TaigaSchema.FIELD_TAIGA_EPIC_ID, e.id);
            struct.put( TaigaSchema.FIELD_TAIGA_EPIC_SUBJECT, e.subject);
            struct.put( TaigaSchema.FIELD_TAIGA_EPIC_STATUS, e.status_extra_info.name);
            struct.put( TaigaSchema.FIELD_TAIGA_EPIC_IS_CLOSED, e.is_closed);
            if(e.user_stories_counts!=null) {
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_PROGRESS, e.user_stories_counts.progress);
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_TOTAL, e.user_stories_counts.total);
            }
            if(e.created_date!=null) struct.put( TaigaSchema.FIELD_TAIGA_EPIC_CREATED_DATE, dfZULU.format(e.created_date));
            if(e.modified_date!=null) struct.put( TaigaSchema.FIELD_TAIGA_EPIC_MODIFIED_DATE, dfZULU.format(e.modified_date));

            Vector<Struct> userstories = new Vector<Struct>();

            for(int x = 0; x < us.length; ++x){
                if(us[x].epics!=null) {
                    if (us[x].epics[0].id.equals(e.id)) {
                        Struct ustemp = new Struct(TaigaSchema.taigaUserStory);
                        ustemp.put("subject", us[x].subject);
                        ustemp.put("id", us[x].id);
                        ustemp.put("status", us[x].status_extra_info.name);
                        ustemp.put("is_closed", us[x].is_closed);
                        if(us[x].assigned_to_extra_info!=null)ustemp.put("assigned", us[x].assigned_to_extra_info.username);
                        ustemp.put("created_date", dfZULU.format(us[x].created_date));
                        if(us[x].modified_date!=null)ustemp.put("modified_date", dfZULU.format(us[x].modified_date));
                        if(us[x].finish_date!=null)ustemp.put("finished_date", dfZULU.format(us[x].finish_date));
                        ustemp.put("total_points", us[x].total_points);
                        for(int y=0; y< milestones.length; ++y) {
                            if(us[x].milestone!=null) {
                                if(us[x].milestone.equals(milestones[y].id)) {
                                    ustemp.put("milestone_id", milestones[y].id);
                                    ustemp.put("milestone_name", milestones[y].name);
                                    ustemp.put("milestone_closed_points", milestones[y].closed_points);
                                    ustemp.put("milestone_total_points", milestones[y].total_points);
                                    ustemp.put("milestone_closed", milestones[y].closed);
                                    ustemp.put("milestone_created_date", dfZULU.format(milestones[y].created_date));
                                    if(milestones[y].modified_date!=null)ustemp.put("milestone_modified_date", dfZULU.format(milestones[y].modified_date));
                                    if(milestones[y].estimated_start!=null)ustemp.put("estimated_start", dfZULU.format(milestones[y].estimated_start));
                                    if(milestones[y].estimated_finish!=null)ustemp.put("estimated_finish", dfZULU.format(milestones[y].estimated_finish));
                                }
                            }
                        }
                        Vector<Struct> task = new Vector<Struct>();
                        for(int y=0; y< tasks.length; ++y) {
                            if(tasks[y].user_story.equals(us[x].id)) {
                                Struct tasktemp = new Struct(TaigaSchema.taigaTask);
                                tasktemp.put("subject", tasks[y].subject);
                                tasktemp.put("id", tasks[y].id);
                                tasktemp.put("status", tasks[y].status_extra_info.name);
                                tasktemp.put("is_closed", tasks[y].is_closed);
                                if(tasks[y].assigned_to_extra_info!=null)tasktemp.put("assigned", tasks[y].assigned_to_extra_info.username);
                                tasktemp.put("created_date", dfZULU.format(tasks[y].created_date));
                                if(tasks[y].modified_date!=null)tasktemp.put("modified_date", dfZULU.format(tasks[y].modified_date));
                                if(tasks[y].finished_date!=null)tasktemp.put("finished_date", dfZULU.format(tasks[y].finished_date));
                                task.add(tasktemp);
                            }
                        }
                        ustemp.put("tasks", task);
                        //TASK I MILESTONES
                        userstories.add(ustemp);
                    }
                }

               /*
            */
            }
            //struct.put( "tasks", task);
            struct.put( TaigaSchema.FIELD_TAIGA_EPIC_USER_STORIES, userstories);

            Map<String,String> sourcePartition = new HashMap<>();
            sourcePartition.put( "taigaUrl", taigaUrl );

            Map<String,String> sourceOffset = new HashMap<>();
            sourceOffset.put( "updated",  dfZULU.format(e.modified_date) );

            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, metrictopic, Schema.STRING_SCHEMA, e.id.toString(), TaigaSchema.taigaEpic , struct);
            result.add(sr);

        }

        log.info("Found " + result.size() + " issues.");

        return result;
    }


    private List<SourceRecord> getIssueSourceRecords(Issue[] redmineIssues, Date updatedSince, Project myProject) {

        List<SourceRecord> result = new ArrayList<>();

        for ( Issue i : redmineIssues) {

            log.info("ISSUE ID: " + i.id);
            log.info("ISSUE URL: " + taigaUrl+"/issues/"+i.id);

            Struct struct = new Struct( TaigaSchema.taigaIssue);
            struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_SUBJECT, i.subject);
            struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_DESCRIPTION, i.description);
            struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_ID, i.id);
            struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_STATUS, i.status_extra_info.name);
            struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_IS_CLOSED, i.is_closed);
            if(i.created_date!=null) struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_CREATED_DATE, dfZULU.format(i.created_date));
            if(i.modified_date!=null) struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_MODIFIED_DATE, dfZULU.format(i.modified_date));
            if(i.finished_date!=null) struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_FINISHED_DATE, dfZULU.format(i.finished_date));
            if(i.assigned_to_extra_info!=null) struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_ASSIGNED, i.assigned_to_extra_info.username);
            int x=0;
            boolean found=false;
            while(!found && x<myProject.severities.length) {
                if(myProject.severities[x].id.equals(i.severity)) {
                    found=true;
                    struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_SEVERITY, myProject.severities[x].name);
                }
                ++x;
            }
            x=0;
            found=false;
            while(!found && x<myProject.priorities.length) {
                if(myProject.priorities[x].id.equals(i.priority)) {
                    found=true;
                    struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_PRIORITY, myProject.priorities[x].name);
                }
                ++x;
            }
            x=0;
            found=false;
            while(!found && x<myProject.issue_types.length) {
                if(myProject.issue_types[x].id.equals(i.type)) {
                    found=true;
                    struct.put( TaigaSchema.FIELD_TAIGA_ISSUE_TYPE, myProject.issue_types[x].name);
                }
                ++x;
            }

            Map<String,String> sourcePartition = new HashMap<>();
            sourcePartition.put( "taigaUrl", taigaUrl );

            Map<String,String> sourceOffset = new HashMap<>();
            sourceOffset.put( "updated",  dfZULU.format(i.modified_date) );

            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, issuetopic, Schema.STRING_SCHEMA, i.id.toString(), TaigaSchema.taigaIssue , struct);
            result.add(sr);

        }

        log.info("Found " + result.size() + " issues.");

        return result;
    }

    @Override
    public void start(Map<String, String> props) {

        log.info("connect-taiga: start");
        log.info(props.toString());

        taigaUrl = props.get( TaigaSourceConfig.TAIGA_URL_CONFIG);
        taigaUser= props.get( TaigaSourceConfig.TAIGA_USER_CONFIG);
        taigaPass = props.get( TaigaSourceConfig.TAIGA_PASS_CONFIG);
        taigaSlug= props.get( TaigaSourceConfig.TAIGA_SLUG_CONFIG);
        taigaProjectId = String.valueOf(TaigaApi.getProjectId(taigaUrl, taigaSlug, taigaUser, taigaPass));
        issuetopic = props.get( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG);
        metrictopic = props.get( TaigaSourceConfig.TAIGA_METRIC_TOPIC_CONFIG);
        taigaInterval = props.get( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG);


        log.info("taiga.url: " + taigaUrl);
        log.info("taiga.interval.seconds: " + taigaInterval);

        if ( (taigaInterval == null || taigaInterval.isEmpty()) ) {
            interval = 3600;
        } else {
            interval = Integer.parseInt(taigaInterval);
        }

        // offsets present?
        Map<String,String> sourcePartition = new HashMap<>();
        sourcePartition.put( "taigaUrl", taigaUrl );

        if ( context != null ) {
            Map<String,Object> offset = context.offsetStorageReader().offset(sourcePartition);
            if (offset != null ) {
                try {
                    mostRecentUpdate = dfZULU.parse( (String) offset.get("updated") );
                    log.info("--------------------------" + "found offset: updated=" + mostRecentUpdate);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    public String version() {
        return version;
    }

}
