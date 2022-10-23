package connect.sheets;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SheetsSourceConnector extends SourceConnector {

    private AuthorizationCredentials authorizationCredentials;

    private String pollInterval;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> properties) {
        pollInterval = properties.get(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG);
        try {
            authorizationCredentials = AuthorizationCredentials.getInstance(
                    properties.get(SheetsSourceConfig.SHEET_CLIENT_ID),
                    properties.get(SheetsSourceConfig.SHEET_PROJECT_ID),
                    properties.get(SheetsSourceConfig.SHEET_AUTH_URI),
                    properties.get(SheetsSourceConfig.SHEET_TOKEN_URI),
                    properties.get(SheetsSourceConfig.SHEET_AUTH_PROVIDER),
                    properties.get(SheetsSourceConfig.SHEET_CLIENT_SECRET),
                    properties.get(SheetsSourceConfig.SHEET_REDIRECT_URI));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
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
        configuration.put(SheetsSourceConfig.SHEET_CLIENT_ID, authorizationCredentials.getClientId());
        configuration.put(SheetsSourceConfig.SHEET_PROJECT_ID, authorizationCredentials.getProjectId());
        configuration.put(SheetsSourceConfig.SHEET_AUTH_URI, authorizationCredentials.getAuthorizationUri().toString());
        configuration.put(SheetsSourceConfig.SHEET_TOKEN_URI, authorizationCredentials.getTokenUri().toString());
        configuration.put(SheetsSourceConfig.SHEET_AUTH_PROVIDER, authorizationCredentials.getAuthorizationProvider().toString());
        configuration.put(SheetsSourceConfig.SHEET_CLIENT_SECRET, authorizationCredentials.getClientSecret());
        configuration.put(SheetsSourceConfig.SHEET_REDIRECT_URI, authorizationCredentials.getRedirectUris().toString());
        configuration.put(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG, "" + pollInterval);
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
