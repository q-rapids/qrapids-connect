/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.github;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

/**
 * Schema Sonarqube Measures & Issues
 * @author wickenkamp
 *
 */
public class GithubSchema {
	
	// Issues
	public static String FIELD_GITHUB_ISSUE_URL = "url";
	public static String FIELD_GITHUB_ISSUE_HTML_URL = "html_url";
	public static String FIELD_GITHUB_ISSUE_ID = "id";
	public static String FIELD_GITHUB_ISSUE_NUMBER = "number";
	public static String FIELD_GITHUB_ISSUE_TITLE = "title";
	public static String FIELD_GITHUB_ISSUE_USER = "user";
	public static String FIELD_GITHUB_ISSUE_LABELS = "labels";
	public static String FIELD_GITHUB_ISSUE_STATE = "state";

	public static String FIELD_GITHUB_ISSUE_ASSIGNEES = "assignees";

	public static String FIELD_GITHUB_ISSUE_CREATED_AT = "created_at";
	public static String FIELD_GITHUB_ISSUE_UPDATED_AT = "updated_at";
	public static String FIELD_GITHUB_ISSUE_CLOSED_AT = "closed_at";

	public static String FIELD_GITHUB_ISSUE_BODY = "body";


	//user
	public static String FIELD_GITHUB_USER_LOGIN = "login";
	public static String FIELD_GITHUB_USER_ID = "id";
	public static String FIELD_GITHUB_USER_URL = "url";
	public static String FIELD_GITHUB_USER_TYPE = "type";
	public static String FIELD_GITHUB_USER_ADMIN = "site_admin";

	/**
	 * Schema Github Issues
	 * Defines data structure of an issue for kafka
	 */

	public static Schema userSchema =  SchemaBuilder.struct().name("user")
			.field(FIELD_GITHUB_USER_LOGIN, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_GITHUB_USER_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_GITHUB_USER_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_GITHUB_USER_TYPE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_GITHUB_USER_ADMIN, Schema.OPTIONAL_STRING_SCHEMA)
			.build();

	public static Schema labelsSchema =  SchemaBuilder.struct().name("labels")
			.field("name", Schema.OPTIONAL_STRING_SCHEMA)
			.build();
	
	public static Schema githubIssue = SchemaBuilder.struct().name("github")

		.field(FIELD_GITHUB_ISSUE_URL, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_HTML_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_ID, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_NUMBER, Schema.OPTIONAL_INT64_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_TITLE, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_USER, userSchema)
		.field(FIELD_GITHUB_ISSUE_LABELS , SchemaBuilder.array(labelsSchema).build())
		.field(FIELD_GITHUB_ISSUE_STATE, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_ASSIGNEES, SchemaBuilder.array(userSchema).build())
		.field(FIELD_GITHUB_ISSUE_CREATED_AT, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_UPDATED_AT, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_CLOSED_AT, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_BODY, Schema.OPTIONAL_STRING_SCHEMA)
		.build();
	
}
