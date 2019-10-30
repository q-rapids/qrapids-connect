package connect.gitlab;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.data.Date;

public class GitlabSchema {
	
	// Gitlab
	public static String FIELD_URL = "gitlabURL";
	public static String FIELD_ISSUE_URL = "gitlabIssueURL";
	public static String FIELD_ISSUE_TITLE = "title";
	public static String FIELD_ISSUE_CREATED_ON = "created_on";
	public static String FIELD_ISSUE_UPDATED_ON = "updated_on";
	public static String FIELD_LABELS = "labels";
	public static String FIELD_STATE = "state";
  
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
			.field(FIELD_ISSUE_TITLE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ISSUE_UPDATED_ON , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_STATE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_LABELS , SchemaBuilder.array(labelsSchema).build())
			.build();
			

}
