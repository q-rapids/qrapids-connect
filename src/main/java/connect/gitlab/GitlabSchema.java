package connect.gitlab;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class GitlabSchema {
	
	// Gitlab
	public static String FIELD_URL = "gitlabURL";
	public static String FIELD_ISSUE_URL = "gitlabIssueURL";
	public static String FIELD_PROJECT_ID = "project_id";
	public static String FIELD_ISSUE_ID = "id";
	public static String FIELD_ISSUE_KEY = "key";
	public static String FIELD_ISSUE_TITLE = "title";
	public static String FIELD_ISSUE_CREATED_AT = "created_at";
	public static String FIELD_ISSUE_UPDATED_AT = "updated_at";
	public static String FIELD_ISSUE_DUE_DATE = "due_date";
	public static String FIELD_ISSUE_CLOSED_AT = "closed_at";
	public static String FIELD_LABELS = "labels";
	public static String FIELD_STATE = "state";
	// times
	public static String FIELD_ISSUE_TIME_ESTIMATE = "time_estimate";
	public static String FIELD_ISSUE_TIME_SPENT = "time_spent";
	// tasks
	public static String FIELD_ISSUE_TASKS_HAS = "tasks_has";
	public static String FIELD_ISSUE_TASKS_TOTAL = "tasks_total";
	public static String FIELD_ISSUE_TASKS_COMPLETED = "tasks_completed";
  
	/**
	* Netset Labels Object 
	*/
	public static Schema labelsSchema =  SchemaBuilder.struct().name("issue_label")
			.field("value", Schema.OPTIONAL_STRING_SCHEMA)
			.build();

	/**
	 * Schema Gitlab Issue
	 */
	public static Schema issueSchema = SchemaBuilder.struct().name("gitlabissue")
			.field(FIELD_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ISSUE_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PROJECT_ID, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_ID, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_KEY, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_TITLE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ISSUE_CREATED_AT, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_UPDATED_AT, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_DUE_DATE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ISSUE_CLOSED_AT, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_STATE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ISSUE_TIME_ESTIMATE, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_ISSUE_TIME_SPENT, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_ISSUE_TASKS_HAS, Schema.BOOLEAN_SCHEMA)
			.field(FIELD_ISSUE_TASKS_TOTAL, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_ISSUE_TASKS_COMPLETED, Schema.OPTIONAL_INT64_SCHEMA)
			.field(FIELD_LABELS , SchemaBuilder.array(labelsSchema).build())
			.build();
}
