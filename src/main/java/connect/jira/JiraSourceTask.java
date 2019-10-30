package connect.jira;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import model.jira.Issue;
import model.jira.JQLResult;



/**
 * Jira Connector Source Task
 * @author wicken
 *
 */
public class JiraSourceTask extends SourceTask {

	private static DateFormat jiraDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	static {
		dfZULU.setTimeZone(tzUTC);
	}

	private String version = "0.0.1";

	private String jiraUrl;
	private String username;
	private String password;
	
	private String topic;
	private int interval;
	
	// query
	private String projectKey;
	private String createdSince;
	
	private String query;
	private String jiraQuery; 
	private String mostRecentUpdate =  "2000-01-01T00:00:00.000+0000";

	private static DateTimeFormatter jiraDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static DateTimeFormatter queryDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm");
	private static DateTimeFormatter isoDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
	
	
	
	
	// millis of last poll
	private long lastPoll = 0;

	private Boolean firstPoll = true;
	
	private Logger log = Logger.getLogger(JiraSourceTask.class.getName());
	
	

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		
		List<SourceRecord> records = new ArrayList<>(); 
		
		log.info("lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval );
		
		if ( lastPoll != 0 ) {
			if ( System.currentTimeMillis() < ( lastPoll + (interval * 1000) ) ) {
				// log.info("----------------------------------------------------------- exit polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
				Thread.sleep(1000);
				return records;
			}
		} 
		
		lastPoll = System.currentTimeMillis();

		int startAt = 0;
		if ( firstPoll ) {
			JQLResult jqlResult;
			do { 
				jqlResult = JiraApi.getIssues(jiraUrl, username, password, query, startAt);
				
				if ( jqlResult.issues.length > 0 ) {
					mostRecentUpdate = jqlResult.issues[jqlResult.issues.length-1].fields.updated;
				}
				
				records.addAll( getIssueSourceRecords(jqlResult) );
				startAt += jqlResult.maxResults;
			} while ( startAt < jqlResult.total );
			
			firstPoll = false;
			
		} else { 
			query = "updated>" + convertToQueryDate(mostRecentUpdate,1);
			
			log.info("Query updated since: " + mostRecentUpdate);
			log.info("query: " + query);
			
			if ( projectKey != null && ! projectKey.isEmpty() ) {
				query += "+AND+project=" + projectKey;
			}
			query += "+order+by+updated+ASC";
			
			JQLResult jqlResult;
			do { 
				jqlResult = JiraApi.getIssues(jiraUrl, username, password, query, startAt);
				
				if ( jqlResult == null ) {
					log.warning("unexpected null result while querying Jira!");
					return records;
				}
				
				if ( jqlResult.issues.length > 0 ) {
					mostRecentUpdate = jqlResult.issues[jqlResult.issues.length - 1].fields.updated;
				}
				
				records.addAll( getIssueSourceRecords(jqlResult) );
				startAt += jqlResult.maxResults;
				
				
			} while ( startAt < jqlResult.total );

		}
	
		return records;

	}

	private List<SourceRecord>  getIssueSourceRecords(JQLResult jqlResult) {
		
		List<SourceRecord> result = new ArrayList<>();
		
		for (Issue i : jqlResult.issues ) {
			
			String resolution=null;
			if ( i.fields.resolution!= null ) {
				resolution = i.fields.resolution.name;
			} 
			
			String assignee = null;
			if ( i.fields.assignee!= null ) {
				assignee = i.fields.assignee.name;
			} 
			
			String priority = null;
			if ( i.fields.priority!= null ) {
				priority = i.fields.priority.name;
			} 

			if ( i.fields.updated.compareTo( mostRecentUpdate ) > 0) {
				mostRecentUpdate = i.fields.updated;
			}

			Struct struct = new Struct( JiraSchema.issueSchema )
					.put( JiraSchema.FIELD_JIRA_URL, jiraUrl )
					.put( JiraSchema.FIELD_JIRA_PROJECT_KEY, projectKey )
					.put( JiraSchema.FIELD_JIRA_PROJECT_NAME, i.fields.project.name )
					.put( JiraSchema.FIELD_JIRA_ISSUE_SELF, i.self )
					.put( JiraSchema.FIELD_JIRA_ISSUE_TYPE, i.fields.issuetype.name )
					.put( JiraSchema.FIELD_JIRA_ISSUE_ID, i.id )
					.put( JiraSchema.FIELD_JIRA_ISSUE_KEY, i.key)
					.put( JiraSchema.FIELD_JIRA_ISSUE_TIMESPENT, i.fields.timespent )
					.put( JiraSchema.FIELD_JIRA_ISSUE_DESCRIPTION, i.fields.description )
					.put( JiraSchema.FIELD_JIRA_ISSUE_AGGTIMESPENT, i.fields.aggregatetimespent )
					.put( JiraSchema.FIELD_JIRA_ISSUE_RESOLUTION, resolution )
					.put( JiraSchema.FIELD_JIRA_ISSUE_AGGTIMEESTIMATE, i.fields.aggregatetimeestimate )
					.put( JiraSchema.FIELD_JIRA_ISSUE_RESOLUTIONDATE, i.fields.resolutiondate )
					.put( JiraSchema.FIELD_JIRA_ISSUE_SUMMARY, i.fields.summary )
					.put( JiraSchema.FIELD_JIRA_ISSUE_LASTVIEWED, i.fields.lastViewed )
					.put( JiraSchema.FIELD_JIRA_ISSUE_CREATOR, i.fields.creator.name )
					.put( JiraSchema.FIELD_JIRA_ISSUE_CREATED,  i.fields.created )
					.put( JiraSchema.FIELD_JIRA_ISSUE_REPORTER, i.fields.reporter.name )
					.put( JiraSchema.FIELD_JIRA_ISSUE_PRIORITY, priority )
				    .put( JiraSchema.FIELD_JIRA_ISSUE_TIMEESTIMATE, i.fields.timeestimate )
					.put( JiraSchema.FIELD_JIRA_ISSUE_DUEDATE, i.fields.duedate  )
					.put( JiraSchema.FIELD_JIRA_ISSUE_ASSIGNEE, assignee )
					.put( JiraSchema.FIELD_JIRA_ISSUE_UPDATED, i.fields.updated )
					.put( JiraSchema.FIELD_JIRA_ISSUE_STATUS, i.fields.status.name );

				Map<String,String> sourcePartition = new HashMap<>();
				sourcePartition.put( "jiraUrl", jiraUrl );
				sourcePartition.put( "projectKey", projectKey );
				
				Map<String,String> sourceOffset = new HashMap<>();
				sourceOffset.put( "updated", mostRecentUpdate );

				// SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, JiraSchema.issueSchema , struct);
				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, Schema.STRING_SCHEMA, i.id, JiraSchema.issueSchema, struct);
				result.add(sr);
				
				
			}
			


		log.info("Found " + result.size() + " issues.");
		
		return result;
	}

	
	@Override
	public void start(Map<String, String> props) {

		log.info("jira-connect-jenkins: start");

		jiraUrl =  props.get( JiraSourceConfig.JIRA_URL_CONFIG );
		username = props.get( JiraSourceConfig.JIRA_USER_CONFIG );
		password = props.get( JiraSourceConfig.JIRA_PASS_CONFIG );
		topic = props.get( JiraSourceConfig.JIRA_TOPIC_CONFIG );
		jiraQuery = props.get( JiraSourceConfig.JIRA_QUERY_CONFIG );
		
		query = "created>2000-01-01";
		
		projectKey = props.get(JiraSourceConfig.JIRA_PROJECT_CONFIG);
		if ( projectKey != null & !projectKey.isEmpty() ) {
			query += "+AND+project=" + projectKey;
		}
		
		createdSince = props.get( JiraSourceConfig.JIRA_CREATED_SINCE_CONFIG);
		if ( createdSince != null && !createdSince.isEmpty() ) {
			query += "+AND+created>=" + createdSince;
		}

		String i = props.get(JiraSourceConfig.JIRA_INTERVAL_SECONDS_CONFIG);
		if ( (i == null || i.isEmpty()) ) {
			interval = 60;
		} else {
			interval = Integer.parseInt(i);
		}
		
		// offsets present?
		Map<String,String> sourcePartition = new HashMap<>();
		sourcePartition.put( "jiraUrl", jiraUrl );
		sourcePartition.put( "projectKey", projectKey );
		
		
		if ( context != null ) {
			Map<String,Object> offset = context.offsetStorageReader().offset(sourcePartition);
			if (offset != null ) {
				String updated = (String) offset.get("updated");
				query += "+AND+updated>" + convertToQueryDate(updated,1);
			
				log.info("--------------------------" + "found offset: updated=" + updated);
			}
			
		}

		query += "+order+by+updated+ASC";

	}

	@Override
	public void stop() {
		// not implemented, necessary?
	}



	public String version() {
		return version;
	}
	
	private  String convertToQueryDate( String jiraDate, int deltaMinutes ) {
		
		LocalDateTime dateTime = LocalDateTime.parse(jiraDate, jiraDateFormatter);
		if (deltaMinutes!=0) {
			dateTime = dateTime.plusMinutes(deltaMinutes);
		}
		
		String result = queryDateFormatter.format(dateTime);
		try {
			result = URLEncoder.encode(queryDateFormatter.format(dateTime),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return "\"" + result + "\"";

	}
	
	private static String convertJiraDateToIso( String jiraStringDate ) {
		
		if ( jiraStringDate==null ) {
			return null;
		}
		
		Date jiraDate;
		try {
			jiraDate = jiraDateFormat.parse(jiraStringDate);
			return  dfZULU.format( jiraDate );
		} catch (ParseException e) {
			return jiraStringDate;
		}
		
	}

}
