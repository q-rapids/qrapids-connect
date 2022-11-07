/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sheets;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

/**
 * Schema Sheets Time measures
 * @author Max Tiessler
 *
 */
public class SheetsSchema {
	
	// Issues
	public static String FIELD_ID;
	public static String FIELD_TEAM_NAME = "team_name";
	public static String FIELD_SPREADSHEET_ID = "spreadsheet_id";
	public static String FIELD_TIME = "time";
	public static String FIELD_DEVELOPER_INFO = "developer_info";
	public static String FIELD_IMPUTATION_TIMES = "imputation_times";

	public static String FIELD_SPRINT_NAME ="sprint_name";

	public static String FIELD_DEVELOPER_NAME = "developer_name";

	public static String FIELD_DEVELOPER_TIME = "total_hours";


	public static Schema imputationSchema = SchemaBuilder.struct().name("imputations")
			.field(FIELD_SPRINT_NAME, Schema.STRING_SCHEMA)
			.field(FIELD_DEVELOPER_TIME, Schema.STRING_SCHEMA)
			.build();

	public static Schema developer =
			SchemaBuilder.struct().name("developers")
					.field(FIELD_DEVELOPER_NAME, Schema.STRING_SCHEMA)
					.field(FIELD_IMPUTATION_TIMES, SchemaBuilder.array(imputationSchema).build())
					.build();

	public static Schema sheetSchema =
			SchemaBuilder.struct().name("sheets")
			.field(FIELD_ID, Schema.STRING_SCHEMA)
			.field(FIELD_TEAM_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_SPREADSHEET_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_TIME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_DEVELOPER_INFO, SchemaBuilder.array(developer).build())
			.build();

}
