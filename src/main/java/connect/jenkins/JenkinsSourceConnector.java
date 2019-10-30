package connect.jenkins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

public class JenkinsSourceConnector extends SourceConnector {
	
	private Logger log = Logger.getLogger(JenkinsSourceConnector.class.getName());
	
	private String jenkinsURL;
	private String jenkinsUser;
	private String jenkinsPass;
	private String jenkinsJobs;
	private String jenkinsTopic;
	private String jenkinsInterval;
	private String preemptiveAuth;


	@Override
	public String version() {
		return "0.0.1";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());
		
		jenkinsURL = props.get( JenkinsSourceConfig.JENKINS_URL_CONFIG );
		jenkinsUser = props.get( JenkinsSourceConfig.JENKINS_USER_CONFIG );
		jenkinsPass = props.get( JenkinsSourceConfig.JENKINS_PASS_CONFIG );
		jenkinsJobs = props.get( JenkinsSourceConfig.JENKINS_JOBS_CONFIG );
		jenkinsTopic = props.get( JenkinsSourceConfig.JENKINS_TOPIC_CONFIG );
		jenkinsInterval = props.get( JenkinsSourceConfig.JENKINS_INTERVAL_SECONDS_CONFIG );
		preemptiveAuth = props.get( JenkinsSourceConfig.JENKINS_AUTH_CONFIG );
		
		if ( jenkinsURL == null || jenkinsURL.isEmpty() )
			throw new ConnectException("JenkinsSourceConnector configuration must include 'jenkins.url' setting");
		
	}

	@Override
	public Class<? extends Task> taskClass() {
		return JenkinsSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put( JenkinsSourceConfig.JENKINS_URL_CONFIG, jenkinsURL );
		  config.put( JenkinsSourceConfig.JENKINS_USER_CONFIG, jenkinsUser );
		  config.put( JenkinsSourceConfig.JENKINS_PASS_CONFIG, jenkinsPass );
		  config.put( JenkinsSourceConfig.JENKINS_TOPIC_CONFIG, jenkinsTopic );
		  config.put( JenkinsSourceConfig.JENKINS_JOBS_CONFIG, jenkinsJobs );
		  config.put( JenkinsSourceConfig.JENKINS_AUTH_CONFIG, preemptiveAuth );
		  config.put( JenkinsSourceConfig.JENKINS_INTERVAL_SECONDS_CONFIG, "" + jenkinsInterval);

		  configs.add(config);
		  return configs;
	}

	@Override
	public void stop() {
		// Nothing to do since JenkinsSourceConnector has no background monitoring.
	}

	@Override
	public ConfigDef config() {
		return JenkinsSourceConfig.DEFS;
	}

}
