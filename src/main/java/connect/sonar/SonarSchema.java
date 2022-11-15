/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonar;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

/**
 * Schema SonarCloud Measures & Issues
 * @author Max Tiessler
 */
public class SonarSchema {
	

	public static String FIELD_SONAR_URL = "sonarUrl";
	public static String FIELD_SONAR_SNAPSHOT_DATE = "snapshotDate";
	
	// Measures
	public static String FIELD_SONAR_MEASURE_BASECOMPONENT_ID = "bcId";
	public static String FIELD_SONAR_MEASURE_BASECOMPONENT_KEY = "bcKey";
	public static String FIELD_SONAR_MEASURE_BASECOMPONENT_NAME = "bcName";
	public static String FIELD_SONAR_MEASURE_BASECOMPONENT_QUALIFIER = "bcQualifier";
	public static String FIELD_SONAR_MEASURE_COMPONENT_ID = "Id";
	public static String FIELD_SONAR_MEASURE_COMPONENT_KEY = "key";
	public static String FIELD_SONAR_MEASURE_COMPONENT_NAME = "name";
	public static String FIELD_SONAR_MEASURE_COMPONENT_QUALIFIER = "qualifier";
	public static String FIELD_SONAR_MEASURE_COMPONENT_PATH = "path";
	public static String FIELD_SONAR_MEASURE_COMPONENT_LANGUAGE = "language";
	public static String FIELD_SONAR_MEASURE_COMPONENT_METRIC = "metric";
	public static String FIELD_SONAR_MEASURE_COMPONENT_VALUE = "value";
	public static String FIELD_SONAR_MEASURE_COMPONENT_FLOATVALUE = "floatvalue";
	
	// Issues
	public static String FIELD_SONAR_ISSUE_KEY = "key";
	public static String FIELD_SONAR_ISSUE_RULE = "rule";
	public static String FIELD_SONAR_ISSUE_SEVERITY = "severity";
	public static String FIELD_SONAR_ISSUE_COMPONENT = "component";
	public static String FIELD_SONAR_ISSUE_COMPONENTID = "componentId";
	public static String FIELD_SONAR_ISSUE_PROJECT = "project";
	public static String FIELD_SONAR_ISSUE_LINE = "line";
	public static String FIELD_SONAR_ISSUE_STARTLINE = "startLine";
	public static String FIELD_SONAR_ISSUE_STARTOFFSET = "startOffset";
	public static String FIELD_SONAR_ISSUE_ENDLINE = "endLine";
	public static String FIELD_SONAR_ISSUE_ENDOFFSET = "endOffset";
	public static String FIELD_SONAR_ISSUE_STATUS = "status";
	public static String FIELD_SONAR_ISSUE_MESSAGE = "message";
	public static String FIELD_SONAR_ISSUE_EFFORT = "effort";
	public static String FIELD_SONAR_ISSUE_DEBT = "debt";
	public static String FIELD_SONAR_ISSUE_AUTHOR = "author";
	public static String FIELD_SONAR_ISSUE_CREATIONDATE = "creationDate";
	

	/**
	 * Schema Sonar Measures
	 */
	public static Schema sonarmeasure = SchemaBuilder.struct().name("sonarmeasure")
			.field(FIELD_SONAR_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_SNAPSHOT_DATE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_BASECOMPONENT_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_BASECOMPONENT_KEY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_BASECOMPONENT_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_BASECOMPONENT_QUALIFIER, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_KEY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_QUALIFIER, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_PATH, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_LANGUAGE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_METRIC , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_VALUE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_MEASURE_COMPONENT_FLOATVALUE , Schema.OPTIONAL_FLOAT32_SCHEMA)
			.build();
	
	
	/**
	 * Schema Sonar Issues
	 */
	public static Schema sonarissue = SchemaBuilder.struct().name("sonarissue")
			.field(FIELD_SONAR_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_SNAPSHOT_DATE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_KEY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_RULE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_SEVERITY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_COMPONENT, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_COMPONENTID, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_SONAR_ISSUE_PROJECT, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_LINE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_SONAR_ISSUE_STARTLINE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_SONAR_ISSUE_STARTOFFSET, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_SONAR_ISSUE_ENDLINE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_SONAR_ISSUE_ENDOFFSET, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_SONAR_ISSUE_STATUS , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_MESSAGE , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_EFFORT , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_DEBT , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_AUTHOR , Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SONAR_ISSUE_CREATIONDATE , Schema.OPTIONAL_STRING_SCHEMA)
			.build();
}
