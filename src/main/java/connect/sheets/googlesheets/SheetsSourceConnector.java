package connect.sheets.googlesheets;

import connect.sheets.AuthorizationCredentials;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.*;
import java.util.logging.Logger;

public class SheetsSourceConnector extends SourceConnector {

    private AuthorizationCredentials authorizationCredentials;

    private String pollInterval;

    private String teamsNum;

    private String teamsInterval;

    private List<String> spreadSheetId;

    private String sprintNames;

    private List<String> sheetsImputationTopic;

    private List<String> teamName;

    private final Logger connectorLogger = Logger.getLogger(SheetsSourceConnector.class.getName());

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> properties) {
        connectorLogger.info("Start method init");

        pollInterval = properties.get(SheetsSourceConfig.SHEETS_INTERVAL_SECONDS_CONFIG);
        teamsInterval = properties.get(SheetsSourceConfig.SHEETS_TEAMS_INTERVAL_CONFIG);
        authorizationCredentials = AuthorizationCredentials.getInstance(properties);

        teamsNum = properties.get(SheetsSourceConfig.SHEETS_TEAMS_NUMBER_CONFIG);
        if (teamsNum == null || Objects.equals( properties.get(SheetsSourceConfig.SHEETS_TEAMS_NUMBER_CONFIG), "") )
            throw new ConnectException("SheetsConnector configuration must include 'sheets.teams.num' setting");
        sprintNames =  properties.get(SheetsSourceConfig.SHEETS_SPRINT_NAMES);
        if (sprintNames == null || Objects.equals( properties.get(SheetsSourceConfig.SHEETS_SPRINT_NAMES), "") )
            throw new ConnectException("SheetsConnector configuration must include 'sprint.names' setting");

        spreadSheetId = new ArrayList<>();
        teamName = new ArrayList<>();
        sheetsImputationTopic = new ArrayList<>();

        int numTeams = Integer.parseInt(teamsNum);
        for (int i = 0; i < numTeams; ++i) {

            String spreadSheetId = properties.get("tasks." + i + "." + SheetsSourceConfig.SPREADSHEETS_ID);
            if (spreadSheetId == null || Objects.equals( properties.get(SheetsSourceConfig.SPREADSHEETS_ID), "") )
                throw new ConnectException("SheetsConnector configuration must include 'spreadsheet.ids' setting");
            this.spreadSheetId.add(spreadSheetId);

            String teamName = properties.get("tasks." + i + "." + SheetsSourceConfig.SHEETS_TEAM_NAME);
            if (teamName == null || Objects.equals( properties.get(SheetsSourceConfig.SHEETS_TEAM_NAME), "") )
                throw new ConnectException("SheetsConnector configuration must include 'team.name' setting");
            this.teamName.add(teamName);

            String sheetsImputationTopic = properties.get("tasks." + i + "." + SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG);
            if (sheetsImputationTopic == null || Objects.equals( properties.get(SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG), "") )
                throw new ConnectException("SheetsConnector configuration must include 'imputations.topic' setting");
            this.sheetsImputationTopic.add(sheetsImputationTopic);
        }

        connectorLogger.info("Start method end");
    }


    @Override
    public Class<? extends Task> taskClass() {
        return SheetsSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        connectorLogger.info("Task configuration init");
        ArrayList<Map<String, String>> configurationList = new ArrayList<>();
        Map<String, String> configuration = new HashMap<>();

        configuration.put(SheetsSourceConfig.SHEETS_SPRINT_NAMES, sprintNames);
        configuration.put(SheetsSourceConfig.SHEETS_PROJECT_ID, authorizationCredentials.getProject_id());
        configuration.put(SheetsSourceConfig.SHEETS_PRIVATE_KEY_ID, authorizationCredentials.getPrivate_key_id());
        configuration.put(SheetsSourceConfig.SHEETS_PRIVATE_KEY, authorizationCredentials.getPrivate_key());
        configuration.put(SheetsSourceConfig.SHEETS_CLIENT_EMAIL, authorizationCredentials.getClient_email());
        configuration.put(SheetsSourceConfig.SHEETS_CLIENT_ID, authorizationCredentials.getClient_id());
        configuration.put(SheetsSourceConfig.SHEETS_AUTH_URI, authorizationCredentials.getAuth_uri());
        configuration.put(SheetsSourceConfig.SHEETS_TOKEN_URI, authorizationCredentials.getToken_uri());
        configuration.put(SheetsSourceConfig.SHEETS_AUTH_PROVIDER_URL, authorizationCredentials.getAuth_provider_x509_cert_url());
        configuration.put(SheetsSourceConfig.SHEETS_CLIENT_CERTIFICATION_URL, authorizationCredentials.getClient_x509_cert_url());
        configuration.put(SheetsSourceConfig.SHEETS_INTERVAL_SECONDS_CONFIG, pollInterval);
        configuration.put(SheetsSourceConfig.SHEETS_TEAMS_NUMBER_CONFIG, teamsNum);
        configuration.put(SheetsSourceConfig.SHEETS_TEAMS_INTERVAL_CONFIG, teamsInterval);

        int numTeams = Integer.parseInt(teamsNum);
        for (int teamID = 0; teamID < numTeams; ++teamID) {
            configuration.put("tasks." + teamID + "." + SheetsSourceConfig.SPREADSHEETS_ID, spreadSheetId.get(teamID));
            configuration.put("tasks." + teamID + "." + SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG, sheetsImputationTopic.get(teamID));
            configuration.put("tasks." + teamID + "." + SheetsSourceConfig.SHEETS_TEAM_NAME, teamName.get(teamID));
        }

        configurationList.add(configuration);
        connectorLogger.info("Task configuration end");
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
