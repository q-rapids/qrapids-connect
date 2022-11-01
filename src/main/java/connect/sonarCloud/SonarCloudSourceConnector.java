/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonarCloud;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Kafka Sonarqube Connector
 * @author Axel Wickenkamp
 *
 */
public class SonarCloudSourceConnector extends SourceConnector {
	
	private Logger log = Logger.getLogger(SonarCloudSourceConnector.class.getName());
	
	private String sonarURL;
	private String sonarUser;
	private String sonarPass;
	
	private String sonarMeasureTopic;
	private String sonarBaseComponentKey;
	private String sonarMetrics;
	
	private String sonarIssueTopic;
	private String sonarProjectKeys;
	
	private String sonarInterval;
	private String sonarSnapshotDate;


	@Override
	public String version() {
		return "0.0.1";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());
		
		sonarURL = props.get( SonarCloudSourceConfig.SONAR_URL_CONFIG);
		sonarUser = props.get( SonarCloudSourceConfig.SONAR_USER_CONFIG );
		sonarPass = props.get( SonarCloudSourceConfig.SONAR_PASS_CONFIG );
		sonarBaseComponentKey = props.get( SonarCloudSourceConfig.SONAR_BCK_CONFIG );
		sonarMeasureTopic = props.get( SonarCloudSourceConfig.SONAR_MEASURE_TOPIC_CONFIG );
		sonarMetrics = props.get( SonarCloudSourceConfig.SONAR_METRIKKEYS_CONFIG );
		sonarProjectKeys = props.get( SonarCloudSourceConfig.SONAR_PROJECT_KEYS_CONFIG );
		sonarIssueTopic = props.get( SonarCloudSourceConfig.SONAR_ISSUE_TOPIC_CONFIG );
		sonarInterval = props.get( SonarCloudSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG );
		sonarSnapshotDate = props.get( SonarCloudSourceConfig.SONAR_SNAPSHOTDATE_CONFIG );
		
		if ( sonarURL == null || sonarURL.isEmpty() )
			throw new ConnectException("SonarqubeSourceConnector configuration must include 'sonar.url' setting");
		
	}

	@Override
	public Class<? extends Task> taskClass() {
		return SonarCloudSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put( SonarCloudSourceConfig.SONAR_URL_CONFIG, sonarURL );
		  config.put( SonarCloudSourceConfig.SONAR_USER_CONFIG, sonarUser );
		  config.put( SonarCloudSourceConfig.SONAR_PASS_CONFIG, sonarPass );
		  config.put( SonarCloudSourceConfig.SONAR_BCK_CONFIG, sonarBaseComponentKey );
		  config.put( SonarCloudSourceConfig.SONAR_MEASURE_TOPIC_CONFIG, sonarMeasureTopic );
		  config.put( SonarCloudSourceConfig.SONAR_METRIKKEYS_CONFIG, sonarMetrics );
		  config.put( SonarCloudSourceConfig.SONAR_PROJECT_KEYS_CONFIG, sonarProjectKeys );
		  config.put( SonarCloudSourceConfig.SONAR_ISSUE_TOPIC_CONFIG, sonarIssueTopic );
		  config.put( SonarCloudSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG, "" + sonarInterval);
		  config.put( SonarCloudSourceConfig.SONAR_SNAPSHOTDATE_CONFIG, sonarSnapshotDate);

		  configs.add(config);
		  return configs;
	}

	@Override
	public void stop() {
	}

	@Override
	public ConfigDef config() {
		return SonarCloudSourceConfig.DEFS;
	}

}
