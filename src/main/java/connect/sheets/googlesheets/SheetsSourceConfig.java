/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sheets.googlesheets;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 *
 * Configuration properties of the Sheet Source Connector
 * @author Max Tiessler
 */
public class SheetsSourceConfig extends AbstractConfig {


    /*----Authentication----*/

    public static final String ACCOUNT_TYPE = "account.type";
    public static final String ACCOUNT_TYPE_CONFIG_DISPLAY = "Account type";
    public static final String ACCOUNT_TYPE_CONFIG_DOC = "Account type to use when connecting to Google Cloud.";

    public static final String SHEETS_PROJECT_ID = "project.id";
    public static final String SHEETS_PROJECT_ID_CONFIG_DISPLAY = "Project ID";
    public static final String SHEETS_PROJECT_ID_CONFIG_DOC = "Project ID to use when connecting to Google Cloud.";

    public static final String SHEETS_PRIVATE_KEY_ID = "private.key.id";
    public static final String SHEETS_PRIVATE_KEY_ID_CONFIG_DISPLAY = "Private key ID";
    public static final String SHEETS_PRIVATE_KEY_ID_CONFIG_DOC = "Private key ID to use when connecting to Google Cloud.";

    public static final String SHEETS_PRIVATE_KEY = "private.key";
    public static final String SHEETS_PRIVATE_KEY_CONFIG_DISPLAY = "Private Key";
    public static final String SHEETS_PRIVATE_KEY_CONFIG_DOC = "Private key to use when connecting to Google Cloud.";

    public static final String SHEETS_CLIENT_EMAIL = "client.email";
    public static final String SHEETS_CLIENT_EMAIL_CONFIG_DISPLAY = "Client email";
    public static final String SHEETS_CLIENT_EMAIL_CONFIG_DOC = "Client email to use when connecting to Google Cloud.";

    public static final String SHEETS_CLIENT_ID = "client.id";
    public static final String SHEETS_CLIENT_ID_CONFIG_DISPLAY = "Client ID";
    public static final String SHEETS_CLIENT_ID_CONFIG_DOC = "Client ID to use when connecting to Google Cloud.";

    public static final String SHEETS_AUTH_URI = "auth.uri";
    public static final String SHEETS_AUTH_URI_CONFIG_DISPLAY = "Authorization URI";
    public static final String SHEETS_AUTH_URI_CONFIG_DOC = "Authorization URI to use when connecting to Google Cloud.";

    public static final String SHEETS_TOKEN_URI = "token.uri";
    public static final String SHEETS_TOKEN_URI_CONFIG_DISPLAY = "Token URI";
    public static final String SHEETS_TOKEN_URI_CONFIG_DOC = "Token URI to use when connecting to Google Cloud.";

    public static final String SHEETS_AUTH_PROVIDER_URL = "auth.provider.x509.cert.url";
    public static final String SHEETS_AUTH_PROVIDER_URL_CONFIG_DISPLAY = "Authorization provider URL";
    public static final String SHEETS_AUTH_PROVIDER_URL_CONFIG_DOC = "Authorization provider URL to use when connecting to Google Cloud.";

    public static final String SHEETS_CLIENT_CERTIFICATION_URL = "client.x509.cert.url";
    public static final String SHEETS_CLIENT_CERTIFICATION_URL_CONFIG_DISPLAY = "Client authorization URL";
    public static final String SHEETS_CLIENT_CERTIFICATION_URL_CONFIG_DOC = "Client authorization URL to use when connecting to Google Cloud.";
    /*---------------------*/

    /*----Subject Information----*/
    public static final String SPREADSHEETS_ID = "spreadsheet.id";
    public static final String SPREADSHEETS_ID_CONFIG_DISPLAY = "Spreadsheet ID";
    public static final String SPREADSHEETS_ID_CONFIG_DOC = "Spreadsheet ID to use when connecting to Google Sheets API.";

    public static final String SHEETS_SPRINT_NAMES = "sprint.names";
    public static final String SHEETS_SPRINT_NAMES_CONFIG_DISPLAY = "Sprint names";
    public static final String SHEETS_SPRINT_NAMES_CONFIG_DOC = "Sprint names of the project to use when connecting to Google Sheets API";

    public static final String SHEETS_TEAM_NAME = "team.name";
    public static final String SHEETS_TEAM_NAME_CONFIG_DISPLAY = "Team name";
    public static final String SHEETS_TEAM_NAME_CONFIG_DOC = "Team name of the project";

    /*---------------------*/

    /*---- Kafka ----*/
    public static final String SHEETS_GROUP = "SHEETS";
    public static final String SHEETS_IMPUTATIONS_TOPIC_CONFIG = "imputations.topic";
    public static final String SHEETS_IMPUTATIONS_TOPIC_CONFIG_DISPLAY = "Topic to persist spreadsheet imputations.";
    public static final String SHEETS_IMPUTATIONS_TOPIC_CONFIG_DOC = "Topic to persist spreadsheet imputations.";
    public static final String SHEETS_IMPUTATIONS_TOPIC_CONFIG_DEFAULT = "imputations.topic";
    public static final String SHEETS_INTERVAL_SECONDS_CONFIG = "sheet.interval.seconds";
    public static final String SHEETS_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String SHEETS_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final Integer SHEETS_INTERVAL_SECONDS_CONFIG_DEFAULT = 86400; //24 h
    /*---------------------*/

    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(SPREADSHEETS_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SPREADSHEETS_ID_CONFIG_DOC,
                        SHEETS_GROUP,
                        1,
                        ConfigDef.Width.SHORT,
                        SPREADSHEETS_ID_CONFIG_DISPLAY)
                .define(ACCOUNT_TYPE,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        ACCOUNT_TYPE_CONFIG_DOC,
                        SHEETS_GROUP,
                        2,
                        ConfigDef.Width.SHORT,
                        ACCOUNT_TYPE_CONFIG_DISPLAY)
                .define(SHEETS_PROJECT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_PROJECT_ID_CONFIG_DOC,
                        SHEETS_GROUP,
                        3,
                        ConfigDef.Width.LONG,
                        SHEETS_PROJECT_ID_CONFIG_DISPLAY)
                .define(SHEETS_PRIVATE_KEY_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_PRIVATE_KEY_ID_CONFIG_DOC,
                        SHEETS_GROUP,
                        4,
                        ConfigDef.Width.LONG,
                        SHEETS_PRIVATE_KEY_ID_CONFIG_DISPLAY)
                .define(SHEETS_PRIVATE_KEY,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_PRIVATE_KEY_CONFIG_DOC,
                        SHEETS_GROUP,
                        5,
                        ConfigDef.Width.LONG,
                        SHEETS_PRIVATE_KEY_CONFIG_DISPLAY)
                .define(SHEETS_CLIENT_EMAIL,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_CLIENT_EMAIL_CONFIG_DOC,
                        SHEETS_GROUP,
                        6,
                        ConfigDef.Width.LONG,
                        SHEETS_CLIENT_EMAIL_CONFIG_DISPLAY)
                .define(SHEETS_CLIENT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_CLIENT_ID_CONFIG_DOC,
                        SHEETS_GROUP,
                        7,
                        ConfigDef.Width.LONG,
                        SHEETS_CLIENT_ID_CONFIG_DISPLAY)
                .define(SHEETS_AUTH_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_AUTH_URI_CONFIG_DOC,
                        SHEETS_GROUP,
                        8,
                        ConfigDef.Width.LONG,
                        SHEETS_AUTH_URI_CONFIG_DISPLAY)
                .define(SHEETS_TOKEN_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_TOKEN_URI_CONFIG_DOC,
                        SHEETS_GROUP,
                        9,
                        ConfigDef.Width.LONG,
                        SHEETS_TOKEN_URI_CONFIG_DISPLAY)
                .define(SHEETS_AUTH_PROVIDER_URL,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_AUTH_PROVIDER_URL_CONFIG_DOC,
                        SHEETS_GROUP,
                        10,
                        ConfigDef.Width.LONG,
                        SHEETS_AUTH_PROVIDER_URL_CONFIG_DISPLAY)
                .define(SHEETS_CLIENT_CERTIFICATION_URL,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_CLIENT_CERTIFICATION_URL_CONFIG_DOC,
                        SHEETS_GROUP,
                        11,
                        ConfigDef.Width.LONG,
                        SHEETS_CLIENT_CERTIFICATION_URL_CONFIG_DISPLAY)
                .define(SHEETS_INTERVAL_SECONDS_CONFIG,
                        ConfigDef.Type.LONG,
                        SHEETS_INTERVAL_SECONDS_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        SHEETS_INTERVAL_SECONDS_CONFIG_DOC,
                        SHEETS_GROUP,
                        12,
                        ConfigDef.Width.SHORT,
                        SHEETS_INTERVAL_SECONDS_CONFIG_DISPLAY)
                .define(SHEETS_SPRINT_NAMES,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEETS_SPRINT_NAMES_CONFIG_DOC,
                        SHEETS_GROUP,
                        13,
                        ConfigDef.Width.LONG,
                        SHEETS_SPRINT_NAMES_CONFIG_DISPLAY)
                .define(SHEETS_IMPUTATIONS_TOPIC_CONFIG,
                        ConfigDef.Type.STRING,
                        SHEETS_IMPUTATIONS_TOPIC_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        SHEETS_IMPUTATIONS_TOPIC_CONFIG_DOC,
                        SHEETS_GROUP,
                        14,
                        ConfigDef.Width.LONG,
                        SHEETS_IMPUTATIONS_TOPIC_CONFIG_DISPLAY)
                .define(SHEETS_TEAM_NAME,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.LOW,
                        SHEETS_TEAM_NAME_CONFIG_DOC,
                        SHEETS_GROUP,
                        15,
                        ConfigDef.Width.LONG,
                        SHEETS_TEAM_NAME_CONFIG_DISPLAY);
    }

    public SheetsSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

}
