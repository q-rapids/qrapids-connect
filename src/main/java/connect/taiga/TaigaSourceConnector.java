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
    private String taigaMetricEpic;
    private String taigaMetricUserStory;
    private String taigaMetricTask;
    private String taigaInterval;
    private String taigaTaskCustomAttributes;
    private String taigaUserstoryCustomAttributes;

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
        taigaIssueTopic = props.get( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG );
        taigaMetricEpic = props.get( TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG );
        taigaMetricUserStory = props.get( TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG );
        taigaMetricTask = props.get( TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG );
        taigaInterval = props.get( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG );
        taigaTaskCustomAttributes = props.get(TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG);
        taigaUserstoryCustomAttributes = props.get(TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG);

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
        config.put( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG, taigaIssueTopic );
        config.put( TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG, taigaMetricEpic );
        config.put( TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG, taigaMetricUserStory );
        config.put( TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG, taigaMetricTask );
        config.put( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG, "" + taigaInterval);
        config.put(TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG, taigaTaskCustomAttributes);
        config.put(TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG, taigaUserstoryCustomAttributes);
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
