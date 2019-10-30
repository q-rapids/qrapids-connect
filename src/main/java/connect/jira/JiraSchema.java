package connect.jira;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class JiraSchema {
	
	// JIRA
	public static String FIELD_JIRA_URL = "jiraUrl";
	public static String FIELD_JIRA_PROJECT_KEY = "projectKey";
	public static String FIELD_JIRA_PROJECT_NAME = "projectName";
	public static String FIELD_JIRA_ISSUE_SELF = "jiraIssueApi";
	public static String FIELD_JIRA_ISSUE_TYPE = "issuetype";
	public static String FIELD_JIRA_ISSUE_ID = "issueid";
	public static String FIELD_JIRA_ISSUE_KEY = "issuekey";
	public static String FIELD_JIRA_ISSUE_TIMESPENT = "timespent";
	public static String FIELD_JIRA_ISSUE_DESCRIPTION = "description";
	public static String FIELD_JIRA_ISSUE_AGGTIMESPENT = "aggregatetimespent";
	public static String FIELD_JIRA_ISSUE_RESOLUTION = "resolution";
	public static String FIELD_JIRA_ISSUE_AGGTIMEESTIMATE = "aggregatetimeestimate";
	public static String FIELD_JIRA_ISSUE_RESOLUTIONDATE = "resolutiondate";
	public static String FIELD_JIRA_ISSUE_SUMMARY = "summary";
	public static String FIELD_JIRA_ISSUE_LASTVIEWED = "lastViewed";
	public static String FIELD_JIRA_ISSUE_CREATOR = "creator";
	public static String FIELD_JIRA_ISSUE_CREATED = "created";
	public static String FIELD_JIRA_ISSUE_REPORTER = "reporter";
	public static String FIELD_JIRA_ISSUE_PRIORITY = "priority";
	public static String FIELD_JIRA_ISSUE_TIMEESTIMATE = "timeestimate";
	public static String FIELD_JIRA_ISSUE_DUEDATE = "duedate";
	public static String FIELD_JIRA_ISSUE_ASSIGNEE = "assignee";
	public static String FIELD_JIRA_ISSUE_UPDATED = "updated";
	public static String FIELD_JIRA_ISSUE_STATUS = "status";
	

	/**
	 * Schema Jira Issue
	 */
	public static Schema issueSchema = SchemaBuilder.struct().name("jiraissue")
			.field(FIELD_JIRA_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_PROJECT_KEY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_PROJECT_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_SELF, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_TYPE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_KEY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_TIMESPENT, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_JIRA_ISSUE_DESCRIPTION, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_AGGTIMESPENT, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_JIRA_ISSUE_RESOLUTION, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_AGGTIMEESTIMATE, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_JIRA_ISSUE_RESOLUTIONDATE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_SUMMARY , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_LASTVIEWED , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_CREATOR , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_CREATED , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_REPORTER , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_PRIORITY , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_TIMEESTIMATE , Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_JIRA_ISSUE_DUEDATE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_ASSIGNEE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_UPDATED , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_JIRA_ISSUE_STATUS , Schema.OPTIONAL_STRING_SCHEMA)
			.build();
			

}
