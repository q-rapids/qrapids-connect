package connect.sheets;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SheetsSourceConnector extends SourceConnector {

    private AuthorizationCredentials authorizationCredentials;

    private String pollInterval;

    private String spreadSheetIds;

    private String memberNumber;

    private String sprintNames;

    private String sheetHourTopic;

    private String teamNames;

    private final Logger connectorLogger = Logger.getLogger(SheetsSourceConnector.class.getName());

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> properties) {
        pollInterval = properties.get(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG);
        spreadSheetIds = properties.get(SheetsSourceConfig.SPREADSHEET_IDS);
        memberNumber =  properties.get(SheetsSourceConfig.SHEET_TEAM_NUMBER_MEMBERS);
        sprintNames =  properties.get(SheetsSourceConfig.SHEET_SPRINT_NAMES);
        sheetHourTopic = properties.get(SheetsSourceConfig.SHEET_HOUR_TOPIC_CONFIG);
        teamNames = properties.get(SheetsSourceConfig.SHEET_TEAM_NAMES);
        authorizationCredentials = AuthorizationCredentials.getInstance(
                properties.get(SheetsSourceConfig.SHEET_TYPE),
                properties.get(SheetsSourceConfig.SHEET_PROJECT_ID),
                properties.get(SheetsSourceConfig.SHEET_PRIVATE_KEY_ID),
                properties.get(SheetsSourceConfig.SHEET_PRIVATE_KEY),
                properties.get(SheetsSourceConfig.SHEET_CLIENT_EMAIL),
                properties.get(SheetsSourceConfig.SHEET_CLIENT_ID),
                properties.get(SheetsSourceConfig.SHEET_AUTH_URI),
                properties.get(SheetsSourceConfig.SHEET_TOKEN_URI),
                properties.get(SheetsSourceConfig.SHEET_AUTH_PROVIDER_URL),
                properties.get(SheetsSourceConfig.SHEET_CLIENT_CERTIFICATION_URL));

        try {
            initializeSpreadsheet(properties);
        } catch (IOException | AuthorizationCredentialsException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeSpreadsheet(final Map<String, String> properties) throws IOException, AuthorizationCredentialsException {
        connectorLogger.info("connect-sheets // CONNECTOR: Initialize Spreadsheet");
        memberNumber = properties.get(SheetsSourceConfig.SHEET_TEAM_NUMBER_MEMBERS);
        sprintNames = properties.get(SheetsSourceConfig.SHEET_SPRINT_NAMES);
        if(SheetsSourceConfig.SPREADSHEET_IDS == null
                || Objects.equals(properties.get(SheetsSourceConfig.SPREADSHEET_IDS), "")) {
            throw new ConnectException("SheetsConnector configuration must include spreadsheet.ids setting");
            /*
            TODO: automatize (almost done, remains sharing file to users)
            spreadSheetId = createSpreadsheet();
            createSheets();
            shareSpreadsheet();
            */
        } else {
            connectorLogger.info("connect-sheets // CONNECTOR: Spreadsheet exists");
            spreadSheetIds = properties.get(SheetsSourceConfig.SPREADSHEET_IDS);
        }
    }


    @Override
    public Class<? extends Task> taskClass() {
        return SheetsSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        ArrayList<Map<String, String>> configurationList = new ArrayList<>();
        Map<String, String> configuration = new HashMap<>();
        configuration.put(SheetsSourceConfig.SPREADSHEET_IDS, spreadSheetIds);
        configuration.put(SheetsSourceConfig.SHEET_SPRINT_NAMES, sprintNames);
        configuration.put(SheetsSourceConfig.SHEET_TEAM_NUMBER_MEMBERS, memberNumber);
        configuration.put(SheetsSourceConfig.SHEET_PROJECT_ID, authorizationCredentials.getProject_id());
        configuration.put(SheetsSourceConfig.SHEET_PRIVATE_KEY_ID, authorizationCredentials.getPrivate_key_id());
        configuration.put(SheetsSourceConfig.SHEET_PRIVATE_KEY, authorizationCredentials.getPrivate_key());
        configuration.put(SheetsSourceConfig.SHEET_CLIENT_EMAIL, authorizationCredentials.getClient_email());
        configuration.put(SheetsSourceConfig.SHEET_CLIENT_ID, authorizationCredentials.getClient_id());
        configuration.put(SheetsSourceConfig.SHEET_AUTH_URI, authorizationCredentials.getAuth_uri());
        configuration.put(SheetsSourceConfig.SHEET_TOKEN_URI, authorizationCredentials.getToken_uri());
        configuration.put(SheetsSourceConfig.SHEET_AUTH_PROVIDER_URL, authorizationCredentials.getAuth_provider_x509_cert_url());
        configuration.put(SheetsSourceConfig.SHEET_CLIENT_CERTIFICATION_URL, authorizationCredentials.getClient_x509_cert_url());
        configuration.put(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG, "" + pollInterval);
        configuration.put(SheetsSourceConfig.SHEET_HOUR_TOPIC_CONFIG, sheetHourTopic);
        configuration.put(SheetsSourceConfig.SHEET_TEAM_NAMES, teamNames);
        configurationList.add(configuration);
        return configurationList;
    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return SheetsSourceConfig.DEFS;
    }
}
