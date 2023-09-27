/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

/**
 * Kafka Sonarqube Connector
 * @author Axel Wickenkamp, Alexandra Volkova
 *
 */
public class GithubSourceConnector extends SourceConnector {
	
	private final Logger log = Logger.getLogger(GithubSourceConnector.class.getName());

	private List<String> githubURL;
	private String githubSecret;
	private String githubUser;
	private String githubPass;	
	private List<String> githubIssuesTopic;
	private List<String> githubCommitsTopic;
	private String githubInterval;
	private String githubTeamsNum;
	private String githubTeamsInterval;
	private String githubCreatedSince;
	private List<String> taigaTaskTopic;

	@Override
	public String version() {
		return "0.0.1";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());

		githubSecret = props.get( GithubSourceConfig.GITHUB_SECRET_CONFIG );
		githubUser = props.get( GithubSourceConfig.GITHUB_USER_CONFIG );
		githubPass = props.get( GithubSourceConfig.GITHUB_PASS_CONFIG );
		githubInterval = props.get( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG );
		githubTeamsNum = props.get( GithubSourceConfig.GITHUB_TEAMS_NUMBER_CONFIG );
		githubTeamsInterval = props.get( GithubSourceConfig.GITHUB_TEAMS_INTERVAL_CONFIG );
		githubCreatedSince = props.get( GithubSourceConfig.GITHUB_CREATED_SINCE_CONFIG );

		githubURL = new ArrayList<>();
		githubIssuesTopic = new ArrayList<>();
		githubCommitsTopic = new ArrayList<>();
		taigaTaskTopic = new ArrayList<>();

		int teamsNum = Integer.parseInt(githubTeamsNum);
		for (int i = 0; i < teamsNum; ++i) {

			String githubURL = props.get( "tasks." + i + "." + GithubSourceConfig.GITHUB_URL_CONFIG );
			String githubIssuesTopic = props.get( "tasks." + i + "." + GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG );
			String githubCommitsTopic = props.get( "tasks." + i + "." + GithubSourceConfig.GITHUB_COMMIT_TOPIC_CONFIG );
			String taigaTaskTopic = props.get( "tasks." + i + "." + GithubSourceConfig.TAIGA_TASK_TOPIC_CONFIG );

			if ( githubURL == null || githubURL.isEmpty() )
				throw new ConnectException("GithubSourceConnector configuration must include 'tasks."
				+ i + "github.url' setting");

			this.githubURL.add(githubURL);
			this.githubIssuesTopic.add(githubIssuesTopic);
			this.githubCommitsTopic.add(githubCommitsTopic);
			this.taigaTaskTopic.add(taigaTaskTopic);
		}

		if ( githubTeamsNum == null || githubTeamsNum.isEmpty() )
			throw new ConnectException("GithubSourceConnector configuration must include 'github.teams.num' setting");
		if ( teamsNum == 0 )
			throw new ConnectException("GithubSourceConnector configuration 'github.teams.num' must be bigger than 0");
	}

	@Override
	public Class<? extends Task> taskClass() {
		return GithubSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {

		ArrayList<Map<String, String>> configs = new ArrayList<>();
		Map<String, String> config = new HashMap<>();

		config.put( GithubSourceConfig.GITHUB_SECRET_CONFIG, githubSecret );
		config.put( GithubSourceConfig.GITHUB_USER_CONFIG, githubUser );
		config.put( GithubSourceConfig.GITHUB_PASS_CONFIG, githubPass );
		config.put( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG, githubInterval );
		config.put( GithubSourceConfig.GITHUB_TEAMS_NUMBER_CONFIG, githubTeamsNum );
		config.put( GithubSourceConfig.GITHUB_TEAMS_INTERVAL_CONFIG, githubTeamsInterval );
		config.put( GithubSourceConfig.GITHUB_CREATED_SINCE_CONFIG, githubCreatedSince );

		int teamsNum = Integer.parseInt(githubTeamsNum);
		for (int i = 0; i < teamsNum; ++i) {
			config.put( "tasks." + i + "." + GithubSourceConfig.GITHUB_URL_CONFIG, githubURL.get(i) );
			config.put( "tasks." + i + "." + GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG, githubIssuesTopic.get(i) );
			config.put( "tasks." + i + "." + GithubSourceConfig.GITHUB_COMMIT_TOPIC_CONFIG, githubCommitsTopic.get(i) );
			config.put( "tasks." + i + "." + GithubSourceConfig.TAIGA_TASK_TOPIC_CONFIG, taigaTaskTopic.get(i) );
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
		return GithubSourceConfig.DEFS;
	}

}
