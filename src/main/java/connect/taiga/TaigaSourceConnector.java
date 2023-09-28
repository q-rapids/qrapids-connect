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
    private String taigaTeamsInterval;
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
        taigaTeamsNum = props.get( TaigaSourceConfig.TAIGA_TEAMS_NUMBER_CONFIG );
        taigaTeamsInterval = props.get( TaigaSourceConfig.TAIGA_TEAMS_INTERVAL_CONFIG );
        taigaTaskCustomAttributes = props.get( TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG );
        taigaUserstoryCustomAttributes = props.get( TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG );

        taigaSlug = new ArrayList<>();
        taigaIssueTopic = new ArrayList<>();
        taigaMetricEpic = new ArrayList<>();
        taigaMetricUserStory = new ArrayList<>();
        taigaMetricTask = new ArrayList<>();

        int teamsNum = Integer.parseInt(taigaTeamsNum);
        for (int i = 0; i < teamsNum; ++i) {

            String taigaSlug = props.get( "tasks." + i + "." + TaigaSourceConfig.TAIGA_SLUG_CONFIG );
            this.taigaSlug.add(taigaSlug);
            if ( taigaSlug == null || taigaSlug.isEmpty() )
                throw new ConnectException("TaigaSourceConnector configuration must include 'tasks." +
                i + ".slug' setting");

            taigaIssueTopic.add( props.get( "tasks." + i + "." + TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG ) );
            taigaMetricEpic.add( props.get( "tasks." + i + "." + TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG ) );
            taigaMetricUserStory.add( props.get( "tasks." + i + "." + TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG ) );
            taigaMetricTask.add( props.get( "tasks." + i + "." + TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG ) );

        }

        if ( taigaURL == null || taigaURL.isEmpty() )
            throw new ConnectException("TaigaSourceConnector configuration must include 'taiga.url' setting");
        if ( taigaTeamsNum == null || taigaTeamsNum.isEmpty() )
            throw new ConnectException("TaigaSourceConnector configuration must include 'taiga.teams.num' setting");
        if (teamsNum == 0)
            throw new ConnectException("TaigaSourceConnector configuration 'taiga.teams.num' must be bigger than 0");
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
        config.put( TaigaSourceConfig.TAIGA_TEAMS_INTERVAL_CONFIG, taigaTeamsInterval );
        config.put( TaigaSourceConfig.TAIGA_TASK_CUSTOM_ATTRIBUTES_CONFIG, taigaTaskCustomAttributes );
        config.put( TaigaSourceConfig.TAIGA_USERSTORY_CUSTOM_ATTRIBUTES_CONFIG, taigaUserstoryCustomAttributes );

        int numTeams = Integer.parseInt(taigaTeamsNum);
        for (int i = 0; i < numTeams; ++i) {

            config.put( "tasks." + i + "." + TaigaSourceConfig.TAIGA_SLUG_CONFIG, taigaSlug.get(i));
            config.put( "tasks." + i + "." + TaigaSourceConfig.TAIGA_ISSUE_TOPIC_CONFIG, taigaIssueTopic.get(i));
            config.put( "tasks." + i + "." + TaigaSourceConfig.TAIGA_EPIC_TOPIC_CONFIG, taigaMetricEpic.get(i));
            config.put( "tasks." + i + "." + TaigaSourceConfig.TAIGA_USERSTORY_TOPIC_CONFIG, taigaMetricUserStory.get(i));
            config.put( "tasks." + i + "." + TaigaSourceConfig.TAIGA_TASK_TOPIC_CONFIG, taigaMetricTask.get(i));
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
