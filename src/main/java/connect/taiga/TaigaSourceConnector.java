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

    private final Logger log = Logger.getLogger(TaigaSourceConnector.class.getName());

    private String taigaURL;
    private List<String> taigaSlug;
    private String taigaUser;
    private String taigaPass;
    private List<String> taigaIssueTopic;
    private List<String> taigaMetricEpic;
    private List<String> taigaMetricUserStory;
    private List<String> taigaMetricTask;
    private String taigaInterval;
    private String taigaTeamsNum;
    private String taigaTaskCustomAttributes;
    private String taigaUserstoryCustomAttributes;

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> props) {
        log.info(props.toString());
        taigaURL = props.get( TaigaSourceConfig.TAIGA_URL_CONFIG );
        taigaUser = props.get( TaigaSourceConfig.TAIGA_USER_CONFIG );
        taigaPass = props.get( TaigaSourceConfig.TAIGA_PASS_CONFIG );
        taigaInterval = props.get( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG );
        taigaTaskCustomAttributes = props.get( TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG );
        taigaUserstoryCustomAttributes = props.get( TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG );
        taigaTeamsNum = props.get( TaigaSourceConfig.TAIGA_TEAMS_NUMBER_CONFIG );

        taigaSlug = new ArrayList<>();
        taigaIssueTopic = new ArrayList<>();
        taigaMetricEpic = new ArrayList<>();
        taigaMetricUserStory = new ArrayList<>();
        taigaMetricTask = new ArrayList<>();

        int teamsNum = Integer.parseInt(taigaTeamsNum);
        for (int i = 0; i < teamsNum; ++i) {
            taigaSlug.add( props.get( TaigaSourceConfig.TAIGA_SLUG_CONFIG + ".task-" + i ) );
            taigaIssueTopic.add( props.get( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG + ".task-" + i ) );
            taigaMetricEpic.add( props.get( TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG + ".task-" + i ) );
            taigaMetricUserStory.add( props.get( TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG + ".task-" + i ) );
            taigaMetricTask.add( props.get( TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG + ".task-" + i ) );
        }

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
        config.put( TaigaSourceConfig.TAIGA_USER_CONFIG, taigaUser );
        config.put( TaigaSourceConfig.TAIGA_PASS_CONFIG, taigaPass );
        config.put( TaigaSourceConfig.TAIGA_INTERVAL_SECONDS_CONFIG, taigaInterval );
        config.put( TaigaSourceConfig.TAIGA_TEAMS_NUMBER_CONFIG, taigaTeamsNum );
        config.put( TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG, taigaTaskCustomAttributes );
        config.put( TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG, taigaUserstoryCustomAttributes );

        int teamsNum = Integer.parseInt(taigaTeamsNum);
        for (int i = 0; i < teamsNum; ++i) {
            config.put( TaigaSourceConfig.TAIGA_SLUG_CONFIG + ".task-" + i, taigaSlug.get(i));
            config.put( TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG+ ".task-" + i, taigaIssueTopic.get(i));
            config.put( TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG + ".task-" + i, taigaMetricEpic.get(i));
            config.put( TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG + ".task-" + i, taigaMetricUserStory.get(i));
            config.put( TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG + ".task-" + i, taigaMetricTask.get(i));
        }

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
