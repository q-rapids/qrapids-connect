/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sheets;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 *
 * Configuration properties of the Sheet Source Connector
 * @author Max Tiessler
 */
public class SheetsSourceConfig extends AbstractConfig {


    public static final String SPREADSHEET_ID = "spreadsheet.id";
    public static final String SPREADSHEET_ID_CONFIG_DISPLAY = "Spreadsheet ID";
    public static final String SPREADSHEET_ID_CONFIG_DOC = "Spreadsheet ID to use when connecting to Google Sheets API.";

    public static final String SHEET_TYPE = "sheet.type";
    public static final String SHEET_TYPE_CONFIG_DISPLAY = "Connection type";
    public static final String SHEET_TYPE_CONFIG_DOC = "Connection type to use when connecting to Google Sheets API.";

    public static final String SHEET_PROJECT_ID = "project.id";
    public static final String SHEET_PROJECT_ID_CONFIG_DISPLAY = "Project ID";
    public static final String SHEET_PROJECT_ID_CONFIG_DOC = "Project ID to use when connecting to Google Sheets API.";

    public static final String SHEET_PRIVATE_KEY_ID = "private.key.id";
    public static final String SHEET_PRIVATE_KEY_ID_CONFIG_DISPLAY = "Private key ID";
    public static final String SHEET_PRIVATE_KEY_ID_CONFIG_DOC = "Private key ID to use when connecting to Google Sheets API.";

    public static final String SHEET_PRIVATE_KEY = "private.key";
    public static final String SHEET_PRIVATE_KEY_CONFIG_DISPLAY = "Private Key";
    public static final String SHEET_PRIVATE_KEY_CONFIG_DOC = "Private key to use when connecting to Google Sheets API.";


    public static final String SHEET_CLIENT_EMAIL = "client.email";
    public static final String SHEET_CLIENT_EMAIL_CONFIG_DISPLAY = "Client email";
    public static final String SHEET_CLIENT_EMAIL_CONFIG_DOC = "Client email to use when connecting to Google Sheets API.";

    public static final String SHEET_CLIENT_ID = "client.id";
    public static final String SHEET_CLIENT_ID_CONFIG_DISPLAY = "Client ID";
    public static final String SHEET_CLIENT_ID_CONFIG_DOC = "Client ID to use when connecting to Google Sheets API.";


    public static final String SHEET_AUTH_URI = "auth.uri";
    public static final String SHEET_AUTH_URI_CONFIG_DISPLAY = "Authorization URI";
    public static final String SHEET_AUTH_URI_CONFIG_DOC = "Authorization URI to use when connecting to Google Sheets API.";

    public static final String SHEET_TOKEN_URI = "token.uri";
    public static final String SHEET_TOKEN_URI_CONFIG_DISPLAY = "Token URI";
    public static final String SHEET_TOKEN_URI_CONFIG_DOC = "Token URI to use when connecting to Google Sheets API.";

    public static final String SHEET_AUTH_PROVIDER_URL = "auth.provider.x509.cert.url";
    public static final String SHEET_AUTH_PROVIDER_URL_CONFIG_DISPLAY = "Authorization provider URL";
    public static final String SHEET_AUTH_PROVIDER_URL_CONFIG_DOC = "Authorization provider URL to use when connecting to Google Sheets API.";

    public static final String SHEET_CLIENT_CERTIFICATION_URL = "client.x509.cert.url";
    public static final String SHEET_CLIENT_CERTIFICATION_URL_CONFIG_DISPLAY = "Client authorization URL";
    public static final String SHEET_CLIENT_CERTIFICATION_URL_CONFIG_DOC = "Client authorization URL to use when connecting to Google Sheets API.";

    public static final String SHEET_INTERVAL_SECONDS_CONFIG = "sheet.interval.seconds";
    public static final String SHEET_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String SHEET_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final Integer SHEET_INTERVAL_SECONDS_CONFIG_DEFAULT = 24 * 60 * 60; //24 h

    public static final String SHEET_MEMBER_NAMES = "member.names";
    public static final String SHEET_MEMBER_NAMES_CONFIG_DISPLAY = "Member names";
    public static final String SHEET_MEMBER_NAMES_CONFIG_DOC = "Member names of the project to use when connecting to Google Sheets API";

    public static final String SHEET_SPRINT_NAMES = "sprint.names";
    public static final String SHEET_SPRINT_NAMES_CONFIG_DISPLAY = "Sprint names";
    public static final String SHEET_SPRINT_NAMES_CONFIG_DOC = "Sprint names of the project to use when connecting to Google Sheets API";

    public static final String SHEET_GROUP = "SHEET";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(SPREADSHEET_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SPREADSHEET_ID_CONFIG_DOC,
                        SHEET_GROUP,
                        1,
                        ConfigDef.Width.SHORT,
                        SPREADSHEET_ID_CONFIG_DISPLAY)
                .define(SHEET_TYPE,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_TYPE_CONFIG_DOC,
                        SHEET_GROUP,
                        2,
                        ConfigDef.Width.SHORT,
                        SHEET_TYPE_CONFIG_DISPLAY)
                .define(SHEET_PROJECT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_PROJECT_ID_CONFIG_DOC,
                        SHEET_GROUP,
                        3,
                        ConfigDef.Width.LONG,
                        SHEET_PROJECT_ID_CONFIG_DISPLAY)
                .define(SHEET_PRIVATE_KEY_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_PRIVATE_KEY_ID_CONFIG_DOC,
                        SHEET_GROUP,
                        4,
                        ConfigDef.Width.LONG,
                        SHEET_PRIVATE_KEY_ID_CONFIG_DISPLAY)
                .define(SHEET_PRIVATE_KEY,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_PRIVATE_KEY_CONFIG_DOC,
                        SHEET_GROUP,
                        5,
                        ConfigDef.Width.LONG,
                        SHEET_PRIVATE_KEY_CONFIG_DISPLAY)
                .define(SHEET_CLIENT_EMAIL,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_CLIENT_EMAIL_CONFIG_DOC,
                        SHEET_GROUP,
                        6,
                        ConfigDef.Width.LONG,
                        SHEET_CLIENT_EMAIL_CONFIG_DISPLAY)
                .define(SHEET_CLIENT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_CLIENT_ID_CONFIG_DOC,
                        SHEET_GROUP,
                        7,
                        ConfigDef.Width.LONG,
                        SHEET_CLIENT_ID_CONFIG_DISPLAY)
                .define(SHEET_AUTH_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_AUTH_URI_CONFIG_DOC,
                        SHEET_GROUP,
                        8,
                        ConfigDef.Width.LONG,
                        SHEET_AUTH_URI_CONFIG_DISPLAY)
                .define(SHEET_TOKEN_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_TOKEN_URI_CONFIG_DOC,
                        SHEET_GROUP,
                        9,
                        ConfigDef.Width.LONG,
                        SHEET_TOKEN_URI_CONFIG_DISPLAY)
                .define(SHEET_AUTH_PROVIDER_URL,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_AUTH_PROVIDER_URL_CONFIG_DOC,
                        SHEET_GROUP,
                        10,
                        ConfigDef.Width.LONG,
                        SHEET_AUTH_PROVIDER_URL_CONFIG_DISPLAY)
                .define(SHEET_CLIENT_CERTIFICATION_URL,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_CLIENT_CERTIFICATION_URL_CONFIG_DOC,
                        SHEET_GROUP,
                        11,
                        ConfigDef.Width.LONG,
                        SHEET_CLIENT_CERTIFICATION_URL_CONFIG_DISPLAY)
                .define(SHEET_INTERVAL_SECONDS_CONFIG,
                        ConfigDef.Type.LONG,
                        SHEET_INTERVAL_SECONDS_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        SHEET_INTERVAL_SECONDS_CONFIG_DOC,
                        SHEET_GROUP,
                        12,
                        ConfigDef.Width.SHORT,
                        SHEET_INTERVAL_SECONDS_CONFIG_DISPLAY)
                .define(SHEET_MEMBER_NAMES,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_MEMBER_NAMES_CONFIG_DOC,
                        SHEET_GROUP,
                        13,
                        ConfigDef.Width.LONG,
                        SHEET_MEMBER_NAMES_CONFIG_DISPLAY)
                .define(SHEET_SPRINT_NAMES,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_SPRINT_NAMES_CONFIG_DOC,
                        SHEET_GROUP,
                        14,
                        ConfigDef.Width.LONG,
                        SHEET_SPRINT_NAMES_CONFIG_DISPLAY);
    }

    public SheetsSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

}
