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
	public static String FIELD_GITHUB_ISSUE_REPOSITORY_URL = "repository_url";
	public static String FIELD_GITHUB_ISSUE_LABELS_URL = "labels_url";
	public static String FIELD_GITHUB_ISSUE_COMMENTS_URL = "comments_url";
	public static String FIELD_GITHUB_ISSUE_EVENTS_URL = "events_url";
	public static String FIELD_GITHUB_ISSUE_HTML_URL = "html_url";
	public static String FIELD_GITHUB_ISSUE_ID = "id";
	public static String FIELD_GITHUB_ISSUE_NODE_ID = "node_id";
	public static String FIELD_GITHUB_ISSUE_NUMBER = "number";
	public static String FIELD_GITHUB_ISSUE_TITLE = "title";
	public static String FIELD_GITHUB_ISSUE_LABELS = "labels";
	public static String FIELD_GITHUB_ISSUE_STATE = "state";
	public static String FIELD_GITHUB_ISSUE_LOCKED = "locked";

	public static String FIELD_GITHUB_ISSUE_COMMENTS = "comments";
	public static String FIELD_GITHUB_ISSUE_CREATED_AT = "created_at";
	public static String FIELD_GITHUB_ISSUE_UPDATED_AT = "updated_at";
	public static String FIELD_GITHUB_ISSUE_CLOSED_AT = "closed_at";
	public static String FIELD_GITHUB_ISSUE_AUTHOR_ASSOCIATION = "author_association";

	public static String FIELD_GITHUB_ISSUE_BODY = "body";

	/**
	 * Schema Github Issues
	 * Defines data structure of an issue for kafka
	 */
	
	public static Schema labelsSchema =  SchemaBuilder.struct().name("label")
			.field("name", Schema.OPTIONAL_STRING_SCHEMA)
			.build();
	
	public static Schema githubIssue = SchemaBuilder.struct().name("github.issue")

		.field(FIELD_GITHUB_ISSUE_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_REPOSITORY_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_LABELS_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_COMMENTS_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_EVENTS_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_HTML_URL, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_ID, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_NODE_ID, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_NUMBER, Schema.INT64_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_TITLE, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_LABELS , SchemaBuilder.array(labelsSchema).build())
		.field(FIELD_GITHUB_ISSUE_STATE, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_LOCKED, Schema.BOOLEAN_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_COMMENTS, Schema.INT64_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_CREATED_AT, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_UPDATED_AT, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_CLOSED_AT, Schema.OPTIONAL_STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_AUTHOR_ASSOCIATION, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_CREATED_AT, Schema.STRING_SCHEMA)
		.field(FIELD_GITHUB_ISSUE_BODY, Schema.OPTIONAL_STRING_SCHEMA)
		.build();
	
}
