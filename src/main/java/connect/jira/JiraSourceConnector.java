package connect.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

public class JiraSourceConnector extends SourceConnector {
	
	private Logger log = Logger.getLogger(JiraSourceConnector.class.getName());
	
	private String jiraURL;
	private String jiraUser;
	private String jiraPass;
	private String jiraProject;
	private String jiraTopic;
	private String jiraInterval;
	private String jiraQuery;


	@Override
	public String version() {
		return "0.0.1";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());
		
		jiraURL = props.get( JiraSourceConfig.JIRA_URL_CONFIG );
		jiraUser = props.get( JiraSourceConfig.JIRA_USER_CONFIG );
		jiraPass = props.get( JiraSourceConfig.JIRA_PASS_CONFIG );
		jiraProject = props.get( JiraSourceConfig.JIRA_PROJECT_CONFIG );
		jiraTopic = props.get( JiraSourceConfig.JIRA_TOPIC_CONFIG );
		jiraInterval = props.get( JiraSourceConfig.JIRA_INTERVAL_SECONDS_CONFIG );
		jiraQuery = props.get( JiraSourceConfig.JIRA_QUERY_CONFIG );
		
		
		if ( jiraURL == null || jiraURL.isEmpty() )
			throw new ConnectException("JenkinsSourceConnector configuration must include 'jira.url' setting");
		
	}

	@Override
	public Class<? extends Task> taskClass() {
		return JiraSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put( JiraSourceConfig.JIRA_URL_CONFIG, jiraURL );
		  config.put( JiraSourceConfig.JIRA_USER_CONFIG, jiraUser );
		  config.put( JiraSourceConfig.JIRA_PASS_CONFIG, jiraPass );
		  config.put( JiraSourceConfig.JIRA_TOPIC_CONFIG, jiraTopic );
		  config.put( JiraSourceConfig.JIRA_PROJECT_CONFIG, jiraProject );
		  config.put( JiraSourceConfig.JIRA_INTERVAL_SECONDS_CONFIG, "" + jiraInterval);
		  config.put( JiraSourceConfig.JIRA_QUERY_CONFIG, jiraQuery );
		  configs.add(config);
		  return configs;
	}

	@Override
	public void stop() {
		// Nothing to do since JenkinsSourceConnector has no background monitoring.
	}

	@Override
	public ConfigDef config() {
		return JiraSourceConfig.DEFS;
	}

}
