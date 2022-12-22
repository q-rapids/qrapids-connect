/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sheets.googlesheets;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

/**
 * Schema Sheets Time measures
 * @author Max Tiessler
 *
 */
public class SheetsSchema {
	
	// Issues
	public static final String TEAM_ID = "team_id";
	public static final String TEAM_NAME = "team_name";
	public static final String SPREADSHEET_ID = "spreadsheet_id";
	public static final String TIMESTAMP = "timestamp";
	public static final String DEVELOPER_NAME = "developer_name";
	public static final String SPRINT_NAME ="sprint_name";
	public static final String TOTAL_HOURS = "total_hours";
	public static final String RE_HOURS = "re_hours";
	public static final String RF_HOURS = "rf_hours";
	public static final String CP_HOURS = "cp_hours";
	public static final String F_HOURS = "f_hours";
	public static final String DES_HOURS = "des_hours";
	public static final String GP_HOURS = "gp_hours";
	public static final String DOC_HOURS = "doc_hours";
	public static final String PRES_HOURS = "pres_hours";



	public static Schema sheetsInputationSchema =
			SchemaBuilder.struct().name("sheetsInputation")
			.field(TEAM_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(TEAM_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(SPREADSHEET_ID, Schema.OPTIONAL_STRING_SCHEMA)
			.field(TIMESTAMP, Schema.OPTIONAL_STRING_SCHEMA)
			.field(DEVELOPER_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(SPRINT_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(TOTAL_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(RE_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(RF_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(CP_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(F_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(DES_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(GP_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(DOC_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(PRES_HOURS, Schema.OPTIONAL_STRING_SCHEMA)
			.build();
}
