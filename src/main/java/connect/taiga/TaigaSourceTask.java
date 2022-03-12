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
import java.time.LocalDateTime;
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
    private String taigaToken;
    //private String taigaRefresh;
    private String taigaProjectId;
    private String issuetopic;
    private String epictopic;
    private String userstorytopic;
    private String tasktopic;
    private String taigaInterval;
    private Integer interval;
    private String taigaTaskCustomAttributes;
    private String taigaUserstoryCustomAttributes;

    private String createdSince;
    private String updatedSince;

    private Date mostRecentUpdate;

    private static DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");

    // millis of last poll
    private long lastPoll = 0;

    private Boolean firstPoll = true;

    private Logger log = Logger.getLogger(TaigaSourceTask.class.getName());

    private Boolean followsPattern(String description) {

        String a="AS", b="I WANT", c="SO THAT";
        description=description.toUpperCase();
        return (description.contains("AS") && description.contains("I WANT") && description.contains("SO THAT")) ||
                (description.contains("COM A") && description.contains("VULL") && description.contains("DE MANERA QUE")) ||
                (description.contains("COMO") && description.contains("QUIERO") && description.contains("DE MANERA QUE"));
    }

    private Boolean hasAcceptanceCriteria(String criteria) {
        return criteria.contains("1.");
    }

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
        //User u = TaigaApi.refreshToken(taigaRefresh);
        //taigaToken = u.auth_token;
        //taigaRefresh =u.refresh;
        //log.info("REFRESH TOKEN: " + taigaRefresh);
        do {
            if(taigaUser.equals("XXXX") || taigaUser.equals(" ") || taigaUser.equals("")) {
                taigaToken = null;
            }
            else {
                taigaToken = TaigaApi.Login(taigaUrl, taigaUser, taigaPass);

            }
            taigaProjectId = String.valueOf(TaigaApi.getProjectId(taigaUrl, taigaSlug, taigaToken));
            myProject = TaigaApi.getProject(taigaUrl, taigaProjectId, taigaToken);
            redmineIssues = TaigaApi.getIssuesByProjectId(taigaUrl, taigaProjectId, taigaToken);
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
        Milestone[] myMilestones = null;
        Map<Integer,String> taskAttributesIDs = new HashMap<>();
        Map<Integer,String> userstoryAttributesIDs = new HashMap<>();

        do {
            myEpics = TaigaApi.getEpicsByProjectID(taigaUrl, taigaProjectId, taigaToken);
            myUserStories = TaigaApi.getUserStroriesByProjectId(taigaUrl, taigaProjectId,taigaToken);
            myTasks = TaigaApi.getTasks(taigaUrl, taigaProjectId,taigaToken);
            try {
                myMilestones = TaigaApi.getMilestonesByProjectId(taigaUrl, taigaProjectId, taigaToken);
            } catch (Exception e) {
                log.info("unable to parse a date");
            }
            taskAttributesIDs = TaigaApi.getCustomAttributesIDs(taigaUrl, "task",taigaProjectId,taigaToken,taigaTaskCustomAttributes.split(","));
            userstoryAttributesIDs=TaigaApi.getCustomAttributesIDs(taigaUrl, "userstory",taigaProjectId,taigaToken, taigaUserstoryCustomAttributes.split(","));
            //records.addAll( getTaigaMetrics(myEpics, myUserStories, myTasks, myMilestones));
            records.addAll( getTaigaEpics(myEpics));
            records.addAll( getTaigaUserStories( myUserStories, myMilestones, userstoryAttributesIDs));
            records.addAll( getTaigaTasks(myTasks, taskAttributesIDs, myMilestones));

            break;
        } while (true);

        mostRecentUpdate = maxUpdatedOn;
        firstPoll = false;

        return records;
    }

    private List<SourceRecord> getTaigaTasks(Task[] tasks,Map<Integer,String> taskAttributesIDs, Milestone[] milestones)  throws InterruptedException{
        List<SourceRecord> result = new ArrayList<>();

        Struct tasktemp;
        for ( Task t : tasks) {
            log.info("TASK ID: " + t.id);
            log.info("TASK URL: " + taigaUrl + "/task/" + t.id);

            tasktemp = new Struct(TaigaSchema.taigaTask);
            tasktemp.put("subject", t.subject);
            if (t.user_story != null) tasktemp.put("user_story_id", t.user_story);
            tasktemp.put("id", t.id);
            tasktemp.put("status", t.status_extra_info.name);
            tasktemp.put("is_closed", t.is_closed);
            tasktemp.put("reference", t.ref);
            if (t.assigned_to_extra_info != null)
                tasktemp.put("assigned", t.assigned_to_extra_info.username);
            tasktemp.put("created_date", dfZULU.format(t.created_date));
            if (t.modified_date != null)
                tasktemp.put("modified_date", dfZULU.format(t.modified_date));
            if (t.finished_date != null)
                tasktemp.put("finished_date", dfZULU.format(t.finished_date));
            //struct.put( "tasks", task);

            Map<String, String> temp = TaigaApi.getCustomAttributes(t.id, "task", taigaUrl, taigaToken, taskAttributesIDs);
            if (temp.get("Estimated Effort") != null){
                tasktemp.put("estimated_effort", Float.parseFloat(temp.get("Estimated Effort")));
            }
            if (temp.get("Actual Effort") != null){
                tasktemp.put("actual_effort", Float.parseFloat(temp.get("Actual Effort")));
            }
            for(int y=0; y< milestones.length; ++y) {
                if (t.milestone != null) {
                    if (t.milestone.equals(milestones[y].id)) {
                        tasktemp.put("milestone_id", milestones[y].id);
                        tasktemp.put("milestone_name", milestones[y].name);
                        tasktemp.put("milestone_closed_points", milestones[y].closed_points);
                        tasktemp.put("milestone_total_points", milestones[y].total_points);
                        LocalDateTime now = LocalDateTime.now();
                        try {
                            Date today = onlyDate.parse(now.toString());
                            if(!milestones[y].closed && (milestones[y].estimated_start.compareTo(today)<0) ) {
                                tasktemp.put("milestone_closed", milestones[y].closed);
                            }
                            else {
                                tasktemp.put("milestone_closed", true);

                            }
                        } catch(ParseException e){
                            //If an error happens we just put the value of closed to de schema.
                            tasktemp.put("milestone_closed", milestones[y].closed);
                            log.info("unable to parse "+now.toString());
                            throw new InterruptedException();
                        }
                        tasktemp.put("milestone_created_date", dfZULU.format(milestones[y].created_date));
                        if (milestones[y].modified_date != null)
                            tasktemp.put("milestone_modified_date", dfZULU.format(milestones[y].modified_date));
                        if (milestones[y].estimated_start != null)
                            tasktemp.put("estimated_start", dfZULU.format(milestones[y].estimated_start));
                        if (milestones[y].estimated_finish != null)
                            tasktemp.put("estimated_finish", dfZULU.format(milestones[y].estimated_finish));
                    }
                }
            }

            Map<String, String> sourcePartition = new HashMap<>();
            sourcePartition.put("taigaUrl", taigaUrl);

            Map<String, String> sourceOffset = new HashMap<>();
            sourceOffset.put("updated", dfZULU.format(t.modified_date));

            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, tasktopic, Schema.STRING_SCHEMA, t.id.toString(), TaigaSchema.taigaTask, tasktemp);
            result.add(sr);
        }
        return result;
    }

    private List<SourceRecord> getTaigaUserStories(UserStory[] us, Milestone[] milestones, Map<Integer,String> userstoryAttributesIDs) throws InterruptedException {
        List<SourceRecord> result = new ArrayList<>();

        Struct ustemp;
        for(int x = 0; x < us.length; ++x){
            log.info("US ID: " + us[x].id);
            log.info("US URL: " + taigaUrl + "/userstory/" + us[x].id);

            ustemp = new Struct(TaigaSchema.taigaUserStory);
            ustemp.put("subject", us[x].subject);
            if(us[x].epics!=null) ustemp.put("epic_id", us[x].epics[0].id);
            ustemp.put("id", us[x].id);
            ustemp.put("status", us[x].status_extra_info.name);
            ustemp.put("is_closed", us[x].is_closed);
            ustemp.put("reference", us[x].ref);
            if(us[x].assigned_to_extra_info!=null)ustemp.put("assigned", us[x].assigned_to_extra_info.username);
            ustemp.put("created_date", dfZULU.format(us[x].created_date));
            if(us[x].modified_date!=null)ustemp.put("modified_date", dfZULU.format(us[x].modified_date));
            if(us[x].finish_date!=null)ustemp.put("finished_date", dfZULU.format(us[x].finish_date));
            ustemp.put("total_points", us[x].total_points);

            model.taiga.UserStory temp = TaigaApi.getUserStroryById(taigaUrl, us[x].id.toString(), taigaToken );
            ustemp.put("pattern", followsPattern(temp.description));


            Map<String, String> tempMap = TaigaApi.getCustomAttributes(us[x].id, "userstory", taigaUrl, taigaToken, userstoryAttributesIDs);
            if (tempMap.get("Acceptance Criteria") != null){
                ustemp.put("acceptance_criteria", hasAcceptanceCriteria(tempMap.get("Acceptance Criteria")));
            }
            else {
                ustemp.put("acceptance_criteria", false);
            }

            if (tempMap.get("Priority") != null){
                ustemp.put("priority", tempMap.get("Priority"));
            }

            for(int y=0; y< milestones.length; ++y) {
                if (us[x].milestone != null) {
                    if (us[x].milestone.equals(milestones[y].id)) {
                        ustemp.put("milestone_id", milestones[y].id);
                        ustemp.put("milestone_name", milestones[y].name);
                        ustemp.put("milestone_closed_points", milestones[y].closed_points);
                        ustemp.put("milestone_total_points", milestones[y].total_points);
                        LocalDateTime now = LocalDateTime.now();
                        try {
                            Date today = onlyDate.parse(now.toString());
                            if(!milestones[y].closed && (milestones[y].estimated_start.compareTo(today)<0) ) {
                                ustemp.put("milestone_closed", milestones[y].closed);
                            }
                            else {
                                ustemp.put("milestone_closed", true);

                            }
                        } catch(ParseException e){
                            //If an error happens we just put the value of closed to de schema.
                            ustemp.put("milestone_closed", milestones[y].closed);
                            log.info("unable to parse "+now.toString());
                            throw new InterruptedException();
                        }
                        ustemp.put("milestone_created_date", dfZULU.format(milestones[y].created_date));
                        if (milestones[y].modified_date != null)
                            ustemp.put("milestone_modified_date", dfZULU.format(milestones[y].modified_date));
                        if (milestones[y].estimated_start != null)
                            ustemp.put("estimated_start", dfZULU.format(milestones[y].estimated_start));
                        if (milestones[y].estimated_finish != null)
                            ustemp.put("estimated_finish", dfZULU.format(milestones[y].estimated_finish));
                    }
                }
            }
        //struct.put( "tasks", task);

        Map<String,String> sourcePartition = new HashMap<>();
        sourcePartition.put( "taigaUrl", taigaUrl );

        Map<String,String> sourceOffset = new HashMap<>();
        sourceOffset.put( "updated",  dfZULU.format(us[x].modified_date) );

        SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, userstorytopic, Schema.STRING_SCHEMA, us[x].id.toString(), TaigaSchema.taigaUserStory , ustemp);
        result.add(sr);
        }
        return result;
    }


    private List<SourceRecord> getTaigaEpics(Epic[] epics) {
        List<SourceRecord> result = new ArrayList<>();

        Struct struct;
        for ( Epic e : epics) {

            log.info("EPIC ID: " + e.id);
            log.info("EPIC URL: " + taigaUrl + "/epics/" + e.id);

            struct = new Struct(TaigaSchema.taigaEpic);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_ID, e.id);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_SUBJECT, e.subject);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_STATUS, e.status_extra_info.name);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_IS_CLOSED, e.is_closed);
            if (e.user_stories_counts != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_PROGRESS, e.user_stories_counts.progress);
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_TOTAL, e.user_stories_counts.total);
            }
            if (e.created_date != null)
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_CREATED_DATE, dfZULU.format(e.created_date));
            if (e.modified_date != null)
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_MODIFIED_DATE, dfZULU.format(e.modified_date));


            //struct.put( "tasks", task);

            Map<String, String> sourcePartition = new HashMap<>();
            sourcePartition.put("taigaUrl", taigaUrl);

            Map<String, String> sourceOffset = new HashMap<>();
            sourceOffset.put("updated", dfZULU.format(e.modified_date));

            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, epictopic, Schema.STRING_SCHEMA, e.id.toString(), TaigaSchema.taigaEpic, struct);
            result.add(sr);
        }
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
        //taigaToken = props.get(TaigaSourceConfig.TAIGA_TOKEN_CONFIG);
        //taigaRefresh = props.get(TaigaSourceConfig.TAIGA_REFRESH_CONFIG);
        taigaSlug= props.get( TaigaSourceConfig.TAIGA_SLUG_CONFIG);
        issuetopic = props.get( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG);
        epictopic = props.get(TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG);
        userstorytopic = props.get(TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG);
        tasktopic = props.get(TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG);
        taigaInterval = props.get( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG);
        taigaTaskCustomAttributes = props.get(TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG);
        taigaUserstoryCustomAttributes = props.get(TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG);

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