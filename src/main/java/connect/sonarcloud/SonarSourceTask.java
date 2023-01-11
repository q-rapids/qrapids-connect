/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license.
 */

package connect.sonarcloud;

import model.sonarcloud.issues.Issue;
import model.sonarcloud.issues.SonarCloudIssuesResult;
import model.sonarcloud.measures.Component;
import model.sonarcloud.measures.Measure;
import model.sonarcloud.measures.SonarCloudMeasuresResult;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Kafka Connector Task for SonarCloud
 * @author Max Tiessler
 */
public class SonarSourceTask extends SourceTask {

	private static final TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	static {
		dfZULU.setTimeZone(tzUTC);
	}
	
	private static final DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

	private String sonarToken;

	private String sonarProjectKeys;
	private String sonarMetricKeys;

	private String sonarMeasureTopic;
	private String sonarIssueTopic;
	private Date snapshotDate;

	private String pollIntervalConfiguration;

	private Integer pollInterval;

	private Long lastPollTime = 0L;
	
	private final Logger taskLogger = LoggerFactory.getLogger(SonarSourceTask.class);

	@Override
	public void start(Map<String, String> props) {

		taskLogger.info("connect-sonarcloud: start");
		sonarToken = props.get(SonarSourceConfig.SONAR_TOKEN_CONFIG);
		sonarProjectKeys = props.get(SonarSourceConfig.SONAR_PROJECT_KEYS_CONFIG);
		sonarMetricKeys = props.get(SonarSourceConfig.SONAR_METRIC_KEYS_CONFIG);
		sonarMeasureTopic = props.get(SonarSourceConfig.SONAR_MEASURE_TOPIC_CONFIG);
		sonarIssueTopic = props.get(SonarSourceConfig.SONAR_ISSUE_TOPIC_CONFIG);
		pollIntervalConfiguration = props.get(SonarSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG);
		String manualSnapshotDate  = props.get( SonarSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		String auxProperties = props.toString();
		taskLogger.info("properties: {}", auxProperties);
		taskLogger.info(SonarSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		taskLogger.info("manual snapshot date: {}", manualSnapshotDate);

		if (manualSnapshotDate == null || manualSnapshotDate.isEmpty()) {
			snapshotDate = new Date();
			taskLogger.info("Using today as snapshotDate.");
		} else {
			taskLogger.info("Using manual snapshotDate: {}", manualSnapshotDate);
			try {
				snapshotDate = ymd.parse(manualSnapshotDate);
			} catch (ParseException e) {
				taskLogger.warn("Config value for snapshotDate could not be parsed.");
				snapshotDate = new Date();
			}
		}

		if (sonarProjectKeys ==null) {
			throw new ConnectException("No base Component and no componentRoot specified, exiting.");
		}
		setPollConfiguration();
	}

	private void setPollConfiguration() {

		if(pollIntervalConfiguration == null || pollIntervalConfiguration.isEmpty()) {
			pollInterval = 3600;
		} else{
			pollInterval = Integer.parseInt(pollIntervalConfiguration);
		}
	}
	@Override
	public List<SourceRecord> poll() throws InterruptedException {

		List<SourceRecord> records = new ArrayList<>();

		String messageTaskPollInfo = "lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPollTime)
				+ " interval:" + pollInterval;
		taskLogger.info("Task Poll {}", messageTaskPollInfo);
		if (lastPollTime != 0) {
			if (System.currentTimeMillis() < ( lastPollTime + (pollInterval * 1000))) {
				Thread.sleep(1000);
				return records;
			}
		}
		lastPollTime = System.currentTimeMillis();
		String snapshotDateString = ymd.format(snapshotDate);

		int page = 0;
		
		if (sonarProjectKeys != null && !sonarProjectKeys.isEmpty()) {
			SonarCloudIssuesResult iResult;
			do {
				page++;
				iResult = SonarApi.getIssues(sonarToken, sonarProjectKeys, page);
				records.addAll(getSonarIssueRecords(iResult, snapshotDateString));
			} while (page * iResult.paging.pageSize < iResult.paging.total);

			page = 0;
			SonarCloudMeasuresResult mResult;
			do {
				page++;
				mResult = SonarApi.getMeasures(sonarToken, sonarProjectKeys, sonarMetricKeys, page);
				records.addAll(getSonarMeasureRecords(mResult, snapshotDateString));
			} while (page * mResult.paging.pageSize < mResult.paging.total);
		}
		return records;
	}

	private List<SourceRecord>  getSonarMeasureRecords(SonarCloudMeasuresResult mResult, String snapshotDateString) {
		
		List<SourceRecord> result = new ArrayList<>();
		for (Component componente : mResult.components) {
			for (Measure m : componente.measures) {
				Struct measure = new Struct(SonarSchema.sonarmeasure);
				measure.put(SonarSchema.FIELD_SONAR_SNAPSHOT_DATE, snapshotDateString);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_ID, mResult.baseComponent.id);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_KEY, mResult.baseComponent.key);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_NAME, mResult.baseComponent.name);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_QUALIFIER, mResult.baseComponent.qualifier);

				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_ID, componente.id);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_KEY, componente.key);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_NAME, componente.name);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_QUALIFIER, componente.qualifier);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_PATH, componente.path);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_LANGUAGE, componente.language);

				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_METRIC, m.metric);
				measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_VALUE, m.value );
				
				if (m.value != null) {
					try {
						float intvalue = Float.parseFloat(m.value);
						measure.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_FLOATVALUE, intvalue );
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				Map<String,String> hm = new HashMap<>();
				hm.put("2", "2");
				SourceRecord sr = new SourceRecord(hm, hm, sonarMeasureTopic, SonarSchema.sonarmeasure , measure);
				result.add(sr);
			}
		}
		taskLogger.info("Found {} metrics", result.size());
		return result;
	}

	

	private List<SourceRecord>  getSonarIssueRecords(SonarCloudIssuesResult iResult, String snapshotDateString) {
		List<SourceRecord> result = new ArrayList<>();
		for (Issue issue : iResult.issues) {
			Struct auxIssue = new Struct(SonarSchema.sonarissue);
			auxIssue.put(SonarSchema.FIELD_SONAR_SNAPSHOT_DATE, snapshotDateString);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_KEY, issue.key);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_RULE, issue.rule);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_SEVERITY, issue.severity);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_COMPONENT, issue.component);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_COMPONENTID, issue.componentId);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_PROJECT, issue.project);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_LINE, issue.line);
			if (issue.textRange!= null) {
				auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_STARTLINE, issue.textRange.startLine);
				auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_STARTOFFSET, issue.textRange.startOffset);
				auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_ENDLINE, issue.textRange.endLine);
				auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_ENDOFFSET, issue.textRange.endOffset);
			}
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_STATUS, issue.status);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_MESSAGE, issue.message);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_EFFORT, issue.effort);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_DEBT, issue.debt);
			auxIssue.put(SonarSchema.FIELD_SONAR_TYPE, issue.type);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_AUTHOR, issue.author);
			auxIssue.put(SonarSchema.FIELD_SONAR_ISSUE_CREATIONDATE, issue.creationDate);

			Map<String,String> m = new HashMap<>();
			m.put("2", "2");
			
			SourceRecord sr = new SourceRecord(m, m, sonarIssueTopic, SonarSchema.sonarissue , auxIssue);
			taskLogger.info("Source record: {}", sr);
			result.add(sr);
		}
		taskLogger.info("Found {} issues ", result.size());
		return result;
	}

	@Override
	public void stop() {
		// nothing for the moment
	}

	public String version() {
		return "0.0.1";
	}

}
