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

    public static final String SHEET_CLIENT_ID = "sheet.client";
    public static final String SHEET_CLIENT_ID_CONFIG_DISPLAY = "Client ID";
    public static final String SHEET_CLIENT_ID_CONFIG_DOC = "Client ID to use when connecting to Google Sheets API.";

    public static final String SHEET_PROJECT_ID = "sheet.project";
    public static final String SHEET_PROJECT_ID_CONFIG_DISPLAY = "Project ID";
    public static final String SHEET_PROJECT_ID_CONFIG_DOC = "Project ID to use when connecting to Google Sheets API.";

    public static final String SHEET_AUTH_URI = "sheet.auth.uri";
    public static final String SHEET_AUTH_URI_CONFIG_DISPLAY = "Authorization URI";
    public static final String SHEET_AUTH_URI_CONFIG_DOC = "Authorization URI to use when connecting to Google Sheets API.";

    public static final String SHEET_TOKEN_URI = "sheet.auth.token";
    public static final String SHEET_TOKEN_URI_CONFIG_DISPLAY = "Token URI";
    public static final String SHEET_TOKEN_URI_CONFIG_DOC = "Token URI to use when connecting to Google Sheets API.";


    public static final String SHEET_AUTH_PROVIDER = "sheet.auth.provider";
    public static final String SHEET_AUTH_PROVIDER_CONFIG_DISPLAY = "Authorization provider URL";
    public static final String SHEET_AUTH_PROVIDER_CONFIG_DOC = "Authorization provider URL to use when connecting to Google Sheets API.";

    public static final String SHEET_CLIENT_SECRET = "sheet.client.secret";
    public static final String SHEET_CLIENT_SECRET_CONFIG_DISPLAY = "Client secret key";
    public static final String SHEET_CLIENT_SECRET_CONFIG_DOC = "Client secret key to use when connecting to Google Sheets API.";

    public static final String SHEET_REDIRECT_URI = "sheet.redirect.uris";
    public static final String SHEET_REDIRECT_URI_CONFIG_DISPLAY = "Redirect URIs";
    public static final String SHEET_REDIRECT_URI_CONFIG_DOC = "Redirect URIs to use when connecting to Google Sheets API.";

    public static final String SHEET_INTERVAL_SECONDS_CONFIG = "sheet.interval.seconds";
    public static final String SHEET_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String SHEET_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final Integer SHEET_INTERVAL_SECONDS_CONFIG_DEFAULT = 24 * 60 * 60;

    public static final String SHEET_GROUP = "SHEET";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(SHEET_CLIENT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_CLIENT_ID_CONFIG_DOC,
                        SHEET_GROUP,
                        1,
                        ConfigDef.Width.LONG,
                        SHEET_CLIENT_ID_CONFIG_DISPLAY)
                .define(SHEET_PROJECT_ID,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_PROJECT_ID_CONFIG_DOC,
                        SHEET_GROUP,
                        2,
                        ConfigDef.Width.LONG,
                        SHEET_PROJECT_ID_CONFIG_DISPLAY)
                .define(SHEET_AUTH_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_AUTH_URI_CONFIG_DOC,
                        SHEET_GROUP,
                        3,
                        ConfigDef.Width.LONG,
                        SHEET_AUTH_URI_CONFIG_DISPLAY)
                .define(SHEET_TOKEN_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_TOKEN_URI_CONFIG_DOC,
                        SHEET_GROUP,
                        4,
                        ConfigDef.Width.LONG,
                        SHEET_TOKEN_URI_CONFIG_DISPLAY)
                .define(SHEET_AUTH_PROVIDER,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_AUTH_PROVIDER_CONFIG_DOC,
                        SHEET_GROUP,
                        5,
                        ConfigDef.Width.LONG,
                        SHEET_AUTH_PROVIDER_CONFIG_DISPLAY)
                .define(SHEET_CLIENT_SECRET,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_CLIENT_SECRET_CONFIG_DOC,
                        SHEET_GROUP,
                        6,
                        ConfigDef.Width.LONG,
                        SHEET_CLIENT_SECRET_CONFIG_DISPLAY)
                .define(SHEET_REDIRECT_URI,
                        ConfigDef.Type.STRING,
                        ConfigDef.Importance.HIGH,
                        SHEET_REDIRECT_URI_CONFIG_DISPLAY,
                        SHEET_GROUP,
                        7,
                        ConfigDef.Width.LONG,
                        SHEET_REDIRECT_URI_CONFIG_DISPLAY)
                .define(SHEET_INTERVAL_SECONDS_CONFIG,
                        ConfigDef.Type.LONG,
                        SHEET_INTERVAL_SECONDS_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        SHEET_INTERVAL_SECONDS_CONFIG_DOC,
                        SHEET_GROUP,
                        8,
                        ConfigDef.Width.SHORT,
                        SHEET_INTERVAL_SECONDS_CONFIG_DISPLAY);
    }

    public SheetsSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

}
