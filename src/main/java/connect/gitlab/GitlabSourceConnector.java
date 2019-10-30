package connect.gitlab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

public class GitlabSourceConnector extends SourceConnector {
	
	private Logger log = Logger.getLogger(GitlabSourceConnector.class.getName());
	
	private String gitlabURL;
	private String gitlabSecret;
	private String gitlabTopic;
	private String gitlabInterval;
	private String gitlabCreatedSince;


	@Override
	public String version() {
		return "0.0.3";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());
		
		gitlabURL = props.get( GitlabSourceConfig.URL_CONFIG );
		gitlabSecret = props.get( GitlabSourceConfig.SECRET_CONFIG );
		gitlabTopic = props.get( GitlabSourceConfig.TOPIC_CONFIG );
		gitlabInterval = props.get( GitlabSourceConfig.INTERVAL_SECONDS_CONFIG );
		gitlabCreatedSince = props.get( GitlabSourceConfig.CREATED_SINCE_CONFIG );
		
		if ( gitlabURL == null || gitlabURL.isEmpty() )
			throw new ConnectException("GitlabSourceConnector configuration must include 'redmine.url' setting");
		
	}

	@Override
	public Class<? extends Task> taskClass() {
		return GitlabSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put( GitlabSourceConfig.URL_CONFIG, gitlabURL );
		  config.put( GitlabSourceConfig.SECRET_CONFIG, gitlabSecret );
		  config.put( GitlabSourceConfig.TOPIC_CONFIG, gitlabTopic );
		  config.put( GitlabSourceConfig.INTERVAL_SECONDS_CONFIG, "" + gitlabInterval);
		  config.put( GitlabSourceConfig.CREATED_SINCE_CONFIG, gitlabCreatedSince );
		  configs.add(config);
		  return configs;
	}

	@Override
	public void stop() {
		// Nothing to do since has no background monitoring.
	}

	@Override
	public ConfigDef config() {
		return GitlabSourceConfig.DEFS;
	}

}
