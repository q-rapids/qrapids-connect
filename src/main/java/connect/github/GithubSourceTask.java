package connect.github;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.*;
import java.text.ParseException;
import java.util.logging.Logger;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import model.github.*;


public class GithubSourceTask extends SourceTask {

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	static {
		dfZULU.setTimeZone(tzUTC);
	}

	private String version = "0.0.1";

	private String githubUrl;
	private String githubSecret;
	private String githubUser;
	private String githubPass;
	private String topic;
	private String githubInterval;
	private Integer interval;

	private String createdSince;
	private String updatedSince;

	private Date mostRecentUpdate;

	private static DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");

	// millis of last poll
	private long lastPoll = 0;

	private Boolean firstPoll = true;

	private Logger log = Logger.getLogger(GithubSourceTask.class.getName());

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
		GithubIssues redmineIssues;
		// if mostRecentUpdate is available from offset -> storage use it

		if(firstPoll){
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
			redmineIssues = GithubApi.getIssues(githubUrl, githubSecret, createdSince, GithubApi.State.ALL, offset);

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

	private List<SourceRecord> getIssueSourceRecords(GithubIssues redmineIssues, Date updatedSince) {

		List<SourceRecord> result = new ArrayList<>();

		for ( Issue i : redmineIssues.issues ) {

			log.info("ISSUE ID: " + i.number);
			log.info("ISSUE URL: " + githubUrl+"/issues/"+i.number);

			Struct struct = new Struct( GithubSchema.githubIssue);
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_URL, i.url);
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_HTML_URL, i.html_url);
			// Issue IDs
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_NUMBER, i.number); // id for the project
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_ID, i.id); // global ID
			// Issue Info
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_TITLE, i.title);
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_BODY, i.body);
			// Dates
			if (i.created_at != null)
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_CREATED_AT, dfZULU.format(i.created_at));
			if (i.updated_at != null)
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_UPDATED_AT, dfZULU.format(i.updated_at));
			if (i.closed_at != null)
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_CLOSED_AT, dfZULU.format(i.closed_at));

			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_STATE, i.state);

			// labels
			Vector<Struct> labels = new Vector<Struct>();
			for(int labelid = 0; labelid < i.labels.length; ++labelid){
				Struct label = new Struct(GithubSchema.labelsSchema);
				label.put("name", i.labels[labelid].name);
				labels.add(label);
			}

			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_LABELS, labels);

			Map<String,String> sourcePartition = new HashMap<>();
			sourcePartition.put( "githubUrl", githubUrl );

			Map<String,String> sourceOffset = new HashMap<>();
			sourceOffset.put( "updated",  dfZULU.format( i.updated_at ) );

			// we use the github id (i.id) as key in the elasticsearch index (_id)
			SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, Schema.STRING_SCHEMA, i.number.toString(), GithubSchema.githubIssue , struct);
			result.add(sr);

		}

		log.info("Found " + result.size() + " issues.");

		return result;
	}
	
	@Override
	public void start(Map<String, String> props) {

		log.info("connect-github: start");
		log.info(props.toString());

		githubUrl		= props.get( GithubSourceConfig.GITHUB_URL_CONFIG);
		githubSecret	= props.get( GithubSourceConfig.GITHUB_SECRET_CONFIG);
		githubUser 		= props.get( GithubSourceConfig.GITHUB_USER_CONFIG );
		githubPass 		= props.get( GithubSourceConfig.GITHUB_PASS_CONFIG );
		topic 			= props.get( GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG );
		createdSince	= props.get( GithubSourceConfig.GITHUB_CREATED_SINCE_CONFIG);
		githubInterval 	= props.get( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG );

		log.info("github.url: " + githubUrl);
		log.info("github.created.since: " + createdSince);
		log.info("github.interval.seconds: " + githubInterval);
		
		if ( (githubInterval == null || githubInterval.isEmpty()) ) {
			interval = 3600;
		} else {
			interval = Integer.parseInt(githubInterval);
		}

		// offsets present?
		Map<String,String> sourcePartition = new HashMap<>();
		sourcePartition.put( "githubUrl", githubUrl );

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
