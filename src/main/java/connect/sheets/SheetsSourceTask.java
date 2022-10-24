package connect.sheets;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SheetsSourceTask extends SourceTask {
    private Integer pollInterval;

    private Long lastPollTime;

    private AuthorizationCredentials authorizationCredentials;
    private final Logger sheetLogger = Logger.getLogger(SheetsSourceTask.class.getName());
    @Override
    public String version() {
        return null;
    }

    @Override
    public void start(Map<String, String> properties) {
        sheetLogger.info("connect-sheets: start");
        sheetLogger.info(properties.toString());
        lastPollTime = 0L;
        if(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG_DEFAULT == null) {
            pollInterval = 3600;
        } else{
            pollInterval = SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG_DEFAULT;
        }
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

    private boolean lostConnection() {
        return System.currentTimeMillis() < (lastPollTime + (pollInterval * 1000));
    }



    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        List <SourceRecord> records = new ArrayList<>();

        if(lastPollTime != 0 && lostConnection()) {
            Thread.sleep(1000);
            return records;
        }

        lastPollTime = System.currentTimeMillis();

        //Reading table
        sheetLogger.info("Reading table from Google Sheets");

        return new ArrayList<>();
    }

    @Override
    public void stop() {

    }
}
