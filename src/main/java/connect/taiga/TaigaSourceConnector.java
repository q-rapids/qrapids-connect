package connect.taiga;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

public class TaigaSourceConnector extends SourceConnector{

    private Logger log = Logger.getLogger(TaigaSourceConnector.class.getName());

    private String taigaURL;
    private String taigaSlug;
    private String taigaUser;
    private String taigaPass;
    private String taigaToken;
    private String taigaRefresh;
    private String taigaIssueTopic;
    private String taigaMetricTopic;
    private String taigaInterval;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {

        log.info(props.toString());

        taigaURL = props.get( TaigaSourceConfig.TAIGA_URL_CONFIG);
        taigaSlug = props.get( TaigaSourceConfig.TAIGA_SLUG_CONFIG);
        taigaUser = props.get( TaigaSourceConfig.TAIGA_USER_CONFIG );
        taigaPass = props.get( TaigaSourceConfig.TAIGA_PASS_CONFIG );
        taigaToken = props.get( TaigaSourceConfig.TAIGA_TOKEN_CONFIG );
        taigaRefresh = props.get( TaigaSourceConfig.TAIGA_REFRESH_CONFIG );
        taigaIssueTopic = props.get( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG );
        taigaMetricTopic = props.get( TaigaSourceConfig.TAIGA_METRIC_TOPIC_CONFIG );
        taigaInterval = props.get( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG );

        if ( taigaURL == null || taigaURL.isEmpty() )
            throw new ConnectException("TaigaSourceConnector configuration must include 'redmine.url' setting");

    }

    @Override
    public Class<? extends Task> taskClass() {
        return TaigaSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();

        Map<String, String> config = new HashMap<>();

        config.put( TaigaSourceConfig.TAIGA_URL_CONFIG, taigaURL );
        config.put( TaigaSourceConfig.TAIGA_SLUG_CONFIG, taigaSlug );
        config.put( TaigaSourceConfig.TAIGA_USER_CONFIG, taigaUser );
        config.put( TaigaSourceConfig.TAIGA_PASS_CONFIG, taigaPass );
        config.put( TaigaSourceConfig.TAIGA_TOKEN_CONFIG, taigaToken );
        config.put( TaigaSourceConfig.TAIGA_REFRESH_CONFIG, taigaRefresh );
        config.put( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG, taigaIssueTopic );
        config.put( TaigaSourceConfig.TAIGA_METRIC_TOPIC_CONFIG, taigaMetricTopic );
        config.put( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG, "" + taigaInterval);
        configs.add(config);
        return configs;
    }

    @Override
    public void stop() {
        // Nothing to do since Connector has no background monitoring.
    }

    @Override
    public ConfigDef config() {
        return TaigaSourceConfig.DEFS;
    }

}
