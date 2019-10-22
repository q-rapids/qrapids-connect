package connect.mantis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

public class MantisSourceConnector extends SourceConnector {

	public static final String MANTIS_DATABASE_URL_CONFIG = "mantis.url";
	public static final String MANTIS_DATABASE_USER_CONFIG = "mantis.user";
	public static final String MANTIS_DATABASE_PASS_CONFIG = "mantis.pass";
	public static final String MANTIS_PROJECT_CONFIG = "mantis.project";
	public static final String NEW_ISSUE_TOPIC_CONFIG = "topic.newissue";
	public static final String UPDATED_ISSUE_TOPIC_CONFIG = "topic.updatedissue";
	public static final String STAT_TOPIC_CONFIG = "topic.stat";
	public static final String POLL_INTERVAL_SECONDS_CONFIG = "poll.interval.seconds";

	private String version = "0.0.1";

	private static final ConfigDef CONFIG_DEF = new ConfigDef()
			.define(MANTIS_DATABASE_URL_CONFIG, Type.STRING, Importance.HIGH, "Mantis DataBase URL.")
			.define(MANTIS_PROJECT_CONFIG, Type.STRING, Importance.HIGH, "Mantis Project.")
			.define(MANTIS_DATABASE_USER_CONFIG, Type.STRING, Importance.HIGH, "Mantis DataBase Username.")
			.define(MANTIS_DATABASE_PASS_CONFIG, Type.STRING, Importance.HIGH, "Mantis DataBase Password.")
			.define(NEW_ISSUE_TOPIC_CONFIG, Type.STRING, Importance.HIGH, "The topic to publish new issues data.")
			.define(UPDATED_ISSUE_TOPIC_CONFIG, Type.STRING, Importance.HIGH, "The topic to publish updated issue data.")
			.define(POLL_INTERVAL_SECONDS_CONFIG, Type.STRING, Importance.LOW, "Polling interval");

	private String mantisURL;
	private String mantisUser;
	private String mantisPass;
	private String mantisProject;
	private String newIssueTopic;
	private String updatedIssueTopic;
	private String statTopic;
	private int interval;

	@Override
	public ConfigDef config() {
		return CONFIG_DEF;
	}

	@Override
	public void start(Map<String, String> props) {
		mantisURL = props.get(MANTIS_DATABASE_URL_CONFIG);
		mantisUser = props.get(MANTIS_DATABASE_USER_CONFIG);
		mantisPass = props.get(MANTIS_DATABASE_PASS_CONFIG);
		mantisProject = props.get(MANTIS_PROJECT_CONFIG);
		newIssueTopic = props.get(NEW_ISSUE_TOPIC_CONFIG);
		updatedIssueTopic = props.get(UPDATED_ISSUE_TOPIC_CONFIG);
		statTopic = props.get(STAT_TOPIC_CONFIG);


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
		return MantisSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int arg0) {

		ArrayList<Map<String, String>> configs = new ArrayList<>();
		
		Map<String, String> config = new HashMap<>();
		config.put(MANTIS_DATABASE_URL_CONFIG, mantisURL);
		config.put(MANTIS_DATABASE_USER_CONFIG, mantisUser);
		config.put(MANTIS_DATABASE_PASS_CONFIG, mantisPass);
		config.put(MANTIS_PROJECT_CONFIG, mantisProject);
		config.put(NEW_ISSUE_TOPIC_CONFIG, newIssueTopic);
		config.put(STAT_TOPIC_CONFIG,statTopic);
		config.put(UPDATED_ISSUE_TOPIC_CONFIG, updatedIssueTopic);
		config.put(POLL_INTERVAL_SECONDS_CONFIG, "" + interval);
		
		configs.add(config);

		return configs;
	}

	@Override
	public String version() {
		return version;
	}

}
