package connect.openproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

public class OpenProjectSourceConnector extends SourceConnector {

	public static final String OPENPROJECT_URL_CONFIG = "openproject.url";
	public static final String OPENPROJECT_APIKEY = "openproject.apikey";
	public static final String OPENPROJECT_PROJECT_CONFIG = "openproject.project";
	public static final String OPENPROJECT_TOPIC_CONFIG = "openproject.topic";
	public static final String POLL_INTERVAL_SECONDS_CONFIG = "poll.interval.seconds";
	public static final String OPENPROJECT_STAT_TOPIC_CONFIG = "openproject.stat.topic";


	private String version = "0.0.1";

	private static final ConfigDef CONFIG_DEF = new ConfigDef()
			.define(OPENPROJECT_URL_CONFIG, Type.STRING, Importance.HIGH, "OpenProject Server URL.")
			.define(OPENPROJECT_APIKEY, Type.STRING, Importance.HIGH, "OpenProject API Access Key.")
			.define(OPENPROJECT_PROJECT_CONFIG, Type.STRING, Importance.HIGH, "OpenProject Project Name.")
			.define(OPENPROJECT_TOPIC_CONFIG, Type.STRING, Importance.HIGH, "Topic.")
			.define(POLL_INTERVAL_SECONDS_CONFIG, Type.STRING, Importance.LOW, "Polling interval");

	private String openprojectURL;
	private String openprojectApiKey;
	private String openprojectProject;
	private String openprojectTopic;
	private String statTopic;
	private int interval;

	@Override
	public ConfigDef config() {
		return CONFIG_DEF;
	}

	@Override
	public void start(Map<String, String> props) {
		openprojectURL = props.get(OPENPROJECT_URL_CONFIG);
		openprojectApiKey = props.get(OPENPROJECT_APIKEY);
		openprojectProject = props.get(OPENPROJECT_PROJECT_CONFIG);
		openprojectTopic = props.get(OPENPROJECT_TOPIC_CONFIG);
		statTopic  = props.get(OPENPROJECT_STAT_TOPIC_CONFIG);

		String intervalSecs = props.get(POLL_INTERVAL_SECONDS_CONFIG);
		if (intervalSecs != null) {
			interval = Integer.parseInt(intervalSecs);
		} else {
			interval = 60;
		}

	}

	@Override
	public void stop() {
		// not implemented

	}

	@Override
	public Class<? extends Task> taskClass() {
		return OpenProjectSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int arg0) {

		ArrayList<Map<String, String>> configs = new ArrayList<>();
		
		Map<String, String> config = new HashMap<>();
		config.put(OPENPROJECT_URL_CONFIG, openprojectURL);
		config.put(OPENPROJECT_APIKEY, openprojectApiKey);
		config.put(OPENPROJECT_PROJECT_CONFIG, openprojectProject);
		config.put(OPENPROJECT_TOPIC_CONFIG, openprojectTopic);
		config.put(OPENPROJECT_STAT_TOPIC_CONFIG, statTopic);
		config.put(POLL_INTERVAL_SECONDS_CONFIG, "" + interval);
		
		configs.add(config);

		return configs;
	}

	@Override
	public String version() {
		return version;
	}

}
