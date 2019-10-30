package connect.svn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

/**
 * Kafka SVN Connector
 * @author wicken
 */
public class SubversionSourceConnector extends SourceConnector {
	
	
	public static final String REPO_URL_CONFIG  = "repository.url";
	public static final String REPO_PATH_CONFIG = "repository.path";
	public static final String REPO_USER_CONFIG = "repository.user";
	public static final String REPO_PASS_CONFIG = "repository.pass";
	public static final String TOPIC_CONFIG = "topic";
	public static final String POLL_INTERVAL_SECONDS_CONFIG = "poll.interval.seconds";
	
	private String version = "0.0.1";
	
	
	private static final ConfigDef CONFIG_DEF = new ConfigDef()
		.define(REPO_URL_CONFIG, Type.STRING, Importance.HIGH, "Repository URL.")
	    .define(REPO_PATH_CONFIG, Type.STRING, Importance.HIGH, "Repository Path.")
	    .define(REPO_USER_CONFIG, Type.STRING, Importance.HIGH, "Repository Username.")
	    .define(REPO_PASS_CONFIG, Type.STRING, Importance.HIGH, "Repository Password.")
	    .define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, "The topic to publish data to")
		.define(POLL_INTERVAL_SECONDS_CONFIG, Type.STRING, Importance.LOW, "Polling interval");

	
	private String repositoryURL;
	private String repositoryPath;
	private String repositoryUser;
	private String repositoryPass;
	private String topic;

	private int interval;

	@Override
	public ConfigDef config() {
		return CONFIG_DEF;
	}

	@Override
	public void start(Map<String, String> props) {
		repositoryURL  = props.get(REPO_URL_CONFIG);
		repositoryPath = props.get(REPO_PATH_CONFIG);
		repositoryUser = props.get(REPO_USER_CONFIG);
		repositoryPass = props.get(REPO_PASS_CONFIG);
		topic          = props.get(TOPIC_CONFIG);
		
		String intervalSecs = props.get(POLL_INTERVAL_SECONDS_CONFIG);
		if ( intervalSecs != null ) {
			interval = Integer.parseInt(intervalSecs);
		} else {
			interval = 60;
		}
	}

	@Override
	public void stop() {
		// not implemented, necessary?
	}

	@Override
	public Class<? extends Task> taskClass() {
		return SubversionSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		
		ArrayList<Map<String, String>> configs = new ArrayList<>();
		Map<String, String> config = new HashMap<>();
		
		if ( repositoryURL != null && repositoryPath != null ) {
			config.put(REPO_URL_CONFIG, repositoryURL);
			config.put(REPO_PATH_CONFIG, repositoryPath);
			config.put(REPO_USER_CONFIG, repositoryUser);
			config.put(REPO_PASS_CONFIG, repositoryPass);
			config.put(TOPIC_CONFIG, topic);
			config.put(POLL_INTERVAL_SECONDS_CONFIG, "" + interval);
		}
		
		configs.add(config);
		
		return configs;
	}

	@Override
	public String version() {
		return version;
	}

}
