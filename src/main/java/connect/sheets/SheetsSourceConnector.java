package connect.sheets;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetsSourceConnector extends SourceConnector {

    private AuthorizationCredentials authorizationCredentials;

    private String pollInterval;

    private String spreadSheetId;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> properties) {
        pollInterval = properties.get(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG);
        spreadSheetId = properties.get(SheetsSourceConfig.SPREADSHEET_ID);
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
    }

    @Override
    public Class<? extends Task> taskClass() {
        return SheetsSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        ArrayList<Map<String, String>> configurationList = new ArrayList<>();

        Map<String, String> configuration = new HashMap<>();
        configuration.put(SheetsSourceConfig.SPREADSHEET_ID, spreadSheetId);
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
