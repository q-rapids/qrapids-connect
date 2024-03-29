package connect.gitlab;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import model.gitlab.*;


public class GitlabSourceTask extends SourceTask {
    
    private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        dfZULU.setTimeZone(tzUTC);
    }

    private String version = "0.0.3";

    private String gitlabUrl;
    private String secret;
    private String topic;
    private int interval;
    private String createdSince;
    private String updatedSince;
    
    private Date mostRecentUpdate;

    private static DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");

    // millis of last poll
    private long lastPoll = 0;

    private Boolean firstPoll = true;
    
    private Logger log = Logger.getLogger(GitlabSourceTask.class.getName());
    
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
        GitlabIssues redmineIssues;
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
            redmineIssues = GitlabApi.getIssues(gitlabUrl, secret, createdSince, updatedSince, offset);
            
            if (maxUpdatedOn == null &&  redmineIssues.issues.length > 0 ) {
                maxUpdatedOn=redmineIssues.issues[0].updated_at;
            }
            
            total_issues += redmineIssues.issues.length;
            // download up to that moment 
            if(redmineIssues.issues.length == 0 || mostRecentUpdate.compareTo(redmineIssues.issues[0].updated_at) >= 0)break;
            records.addAll( getIssueSourceRecords(redmineIssues, mostRecentUpdate) );
            offset += 1;
        } while (true);
        
        mostRecentUpdate = maxUpdatedOn;
        firstPoll = false;
        
        return records;
    }

    private List<SourceRecord>  getIssueSourceRecords(GitlabIssues redmineIssues, Date updatedSince) {
        
        List<SourceRecord> result = new ArrayList<>();
        
        for ( Issue i : redmineIssues.issues ) {

            log.info("ISSUE ID: " + i.iid);
            log.info("ISSUE URL: " + gitlabUrl+"/issues/"+i.iid);

            Struct struct = new Struct( GitlabSchema.issueSchema);
            struct.put( GitlabSchema.FIELD_URL, gitlabUrl);
            struct.put( GitlabSchema.FIELD_ISSUE_URL, gitlabUrl+"/issues/"+i.iid);
            struct.put( GitlabSchema.FIELD_PROJECT_ID, i.project_id.toString());
            // Issue IDs
            struct.put( GitlabSchema.FIELD_ISSUE_ID, i.iid.toString()); // id for the project
            struct.put( GitlabSchema.FIELD_ISSUE_KEY, i.id.toString()); // global ID
            // Issue Info
            struct.put( GitlabSchema.FIELD_ISSUE_TITLE, i.title);
            // Dates
            if (i.created_at != null)
                struct.put( GitlabSchema.FIELD_ISSUE_CREATED_AT, dfZULU.format(i.created_at));
            if (i.updated_at != null)
                struct.put( GitlabSchema.FIELD_ISSUE_UPDATED_AT, dfZULU.format(i.updated_at));
            if (i.due_date != null)
                struct.put(GitlabSchema.FIELD_ISSUE_DUE_DATE, dfZULU.format(i.due_date));
            if (i.closed_at != null)
                struct.put( GitlabSchema.FIELD_ISSUE_CLOSED_AT, dfZULU.format(i.closed_at));

            struct.put( GitlabSchema.FIELD_STATE, i.state.toString());

            // times
            struct.put( GitlabSchema.FIELD_ISSUE_TIME_ESTIMATE, i.time_stats.time_estimate );
            struct.put( GitlabSchema.FIELD_ISSUE_TIME_SPENT, i.time_stats.total_time_spent );
            // tasks
            struct.put( GitlabSchema.FIELD_ISSUE_TASKS_HAS, i.has_tasks);
            if(i.has_tasks) {
                struct.put(GitlabSchema.FIELD_ISSUE_TASKS_TOTAL, i.task_completion_status.n_tasks);
                struct.put(GitlabSchema.FIELD_ISSUE_TASKS_COMPLETED, i.task_completion_status.n_completed_tasks);
            }
            // labels
            Vector<Struct> labels = new Vector<Struct>();
            for(int labelid = 0; labelid < i.labels.length; ++labelid){
                Struct label = new Struct(GitlabSchema.labelsSchema);
                label.put("value", i.labels[labelid]);
                labels.add(label);
            }
            
            struct.put( GitlabSchema.FIELD_LABELS, labels);

            Map<String,String> sourcePartition = new HashMap<>();
            sourcePartition.put( "gitlabUrl", gitlabUrl );

            Map<String,String> sourceOffset = new HashMap<>();
            sourceOffset.put( "updated",  dfZULU.format( i.updated_at ) );

            // we use the gitlab id (i.id) as key in the elasticsearch index (_id)
            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, Schema.STRING_SCHEMA, i.id.toString(), GitlabSchema.issueSchema , struct);
            result.add(sr);

        }

        log.info("Found " + result.size() + " issues.");
        
        return result;
    }

    
    @Override
    public void start(Map<String, String> props) {

        log.info("connect-gitlab: start");

        gitlabUrl =  props.get( GitlabSourceConfig.URL_CONFIG );
        secret = props.get( GitlabSourceConfig.SECRET_CONFIG );
        topic = props.get( GitlabSourceConfig.TOPIC_CONFIG );
        createdSince = props.get( GitlabSourceConfig.CREATED_SINCE_CONFIG);
        
        log.info("gitlab.url:" + gitlabUrl);
        log.info("gitlab.created.since:" + createdSince);

        String i = props.get(GitlabSourceConfig.INTERVAL_SECONDS_CONFIG);
        
        if ( (i == null || i.isEmpty()) ) {
            interval = 60*60;
        } else {
            interval = Integer.parseInt(i);
        }
        
        // offsets present?
        Map<String,String> sourcePartition = new HashMap<>();
        sourcePartition.put( "gitlabUrl", gitlabUrl );
        
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
        // not implemented, necessary?
    }

    public String version() {
        return version;
    }

}
