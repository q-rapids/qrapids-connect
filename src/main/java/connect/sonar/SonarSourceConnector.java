/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonar;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Kafka SonarCloud Connector
 * @author Max Tiessler
 *
 */
public class SonarSourceConnector extends SourceConnector {
	
	private final Logger log = LoggerFactory.getLogger(SonarSourceConnector.class);
	
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
		String propsAux = props.toString();
		log.info("properties: {}", propsAux);
		if (props.get(SonarSourceConfig.SONAR_TOKEN_CONFIG) == null
				|| props.get(SonarSourceConfig.SONAR_TOKEN_CONFIG).isEmpty())
			throw new ConnectException("SonarqubeSourceConnector configuration must include 'cloud.token' setting");
		cloudToken = props.get(SonarSourceConfig.SONAR_TOKEN_CONFIG);
		cloudOrganizationName = props.get(SonarSourceConfig.SONAR_ORGANIZATION_NAME_CONFIG);
		cloudOrganizationKey = props.get(SonarSourceConfig.SONAR_ORGANIZATION_KEY_CONFIG);
		cloudProjectKeys = props.get(SonarSourceConfig.SONAR_PROJECT_KEYS_CONFIG);
		cloudMetricKeys = props.get(SonarSourceConfig.SONAR_METRIC_KEYS_CONFIG);
		sonarMeasureTopic = props.get(SonarSourceConfig.SONAR_MEASURE_TOPIC_CONFIG);
		sonarIssueTopic = props.get(SonarSourceConfig.SONAR_ISSUE_TOPIC_CONFIG);
		sonarInterval = props.get(SonarSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG);
		sonarSnapshotDate = props.get(SonarSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);

	}

	@Override
	public Class<? extends Task> taskClass() {
		return SonarSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		  ArrayList<Map<String, String>> configs = new ArrayList<>();
		  
		  Map<String, String> config = new HashMap<>();
		  
		  config.put(SonarSourceConfig.SONAR_TOKEN_CONFIG, cloudToken);
		  config.put(SonarSourceConfig.SONAR_ORGANIZATION_NAME_CONFIG, cloudOrganizationName);
		  config.put(SonarSourceConfig.SONAR_ORGANIZATION_KEY_CONFIG, cloudOrganizationKey);
		  config.put(SonarSourceConfig.SONAR_PROJECT_KEYS_CONFIG, cloudProjectKeys);
		  config.put(SonarSourceConfig.SONAR_MEASURE_TOPIC_CONFIG, sonarMeasureTopic);
		  config.put(SonarSourceConfig.SONAR_METRIC_KEYS_CONFIG, cloudMetricKeys);
		  config.put(SonarSourceConfig.SONAR_ISSUE_TOPIC_CONFIG, sonarIssueTopic);
		  config.put(SonarSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG, "" + sonarInterval);
		  config.put(SonarSourceConfig.SONAR_SNAPSHOTDATE_CONFIG, sonarSnapshotDate);

		  configs.add(config);
		  return configs;
	}

	@Override
	public void stop() {
		//Nothing for the moment
	}

	@Override
	public ConfigDef config() {
		return SonarSourceConfig.DEFS;
	}

}
