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
 * Kafka SonarCloud Connector
 * @author Max Tiessler
 *
 */
public class SonarCloudSourceConnector extends SourceConnector {
	
	private Logger log = Logger.getLogger(SonarCloudSourceConnector.class.getName());
	
	private String cloudToken;
	private String cloudOrganizationName;
	private String cloudOrganizationKey;
	private String cloudProjectKeys;
	private String cloudMetricKeys;
	
	private String sonarMeasureTopic;
	private String sonarIssueTopic;
	private String sonarInterval;
	private String sonarSnapshotDate;


	@Override
	public String version() {
		return "0.0.1";
	}

	@Override
	public void start(Map<String, String> props) {
		
		log.info(props.toString());
		cloudToken = props.get(SonarCloudSourceConfig.CLOUD_TOKEN_CONFIG);
		cloudOrganizationName = props.get(SonarCloudSourceConfig.CLOUD_ORGANIZATION_NAME_CONFIG);
		cloudOrganizationKey = props.get(SonarCloudSourceConfig.CLOUD_ORGANIZATION_KEY_CONFIG);
		cloudProjectKeys = props.get(SonarCloudSourceConfig.CLOUD_PROJECT_KEYS_CONFIG);
		cloudMetricKeys = props.get(SonarCloudSourceConfig.CLOUD_METRIC_KEYS_CONFIG);
		sonarMeasureTopic = props.get(SonarCloudSourceConfig.CLOUD_MEASURE_TOPIC_CONFIG);
		sonarIssueTopic = props.get(SonarCloudSourceConfig.CLOUD_ISSUE_TOPIC_CONFIG);
		sonarInterval = props.get(SonarCloudSourceConfig.CLOUD_INTERVAL_SECONDS_CONFIG);
		sonarSnapshotDate = props.get(SonarCloudSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		if ( cloudToken == null || cloudToken.isEmpty() )
			throw new ConnectException("SonarqubeSourceConnector configuration must include 'cloud.token' setting");
	}

	@Override
	public Class<? extends Task> taskClass() {
		return SonarCloudSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put(SonarCloudSourceConfig.CLOUD_TOKEN_CONFIG, cloudToken);
		  config.put(SonarCloudSourceConfig.CLOUD_ORGANIZATION_NAME_CONFIG, cloudOrganizationName);
		  config.put(SonarCloudSourceConfig.CLOUD_ORGANIZATION_KEY_CONFIG, cloudOrganizationKey);
		  config.put(SonarCloudSourceConfig.CLOUD_PROJECT_KEYS_CONFIG, cloudProjectKeys);
		  config.put(SonarCloudSourceConfig.CLOUD_MEASURE_TOPIC_CONFIG, sonarMeasureTopic);
		  config.put(SonarCloudSourceConfig.CLOUD_METRIC_KEYS_CONFIG, cloudMetricKeys);
		  config.put(SonarCloudSourceConfig.CLOUD_ISSUE_TOPIC_CONFIG, sonarIssueTopic);
		  config.put(SonarCloudSourceConfig.CLOUD_INTERVAL_SECONDS_CONFIG, "" + sonarInterval);
		  config.put(SonarCloudSourceConfig.SONAR_SNAPSHOTDATE_CONFIG, sonarSnapshotDate);

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
