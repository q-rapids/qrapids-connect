/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license.
 */

package connect.sonar;

import model.sonarqube.issues.Issue;
import model.sonarqube.issues.SonarIssuesResult;
import model.sonarqube.measures.Component;
import model.sonarqube.measures.Measure;
import model.sonarqube.measures.SonarMeasuresResult;
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

	private Integer interval;

	
	// millis of last poll
	private long lastPoll = 0;

	
	private final Logger log = LoggerFactory.getLogger(SonarSourceTask.class);

	@Override
	public void start(Map<String, String> props) {

		log.info("connect-sonarqube: start");
		sonarToken = props.get(SonarSourceConfig.SONAR_TOKEN_CONFIG);
		sonarProjectKeys = props.get(SonarSourceConfig.SONAR_PROJECT_KEYS_CONFIG);
		sonarMetricKeys = props.get(SonarSourceConfig.SONAR_METRIC_KEYS_CONFIG);
		sonarMeasureTopic = props.get(SonarSourceConfig.SONAR_MEASURE_TOPIC_CONFIG);
		sonarIssueTopic = props.get(SonarSourceConfig.SONAR_ISSUE_TOPIC_CONFIG);
		String sonarInterval = props.get(SonarSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG);
		String manualSnapshotDate  = props.get( SonarSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		String auxProperties = props.toString();
		log.info("properties: {}", auxProperties);
		log.info(SonarSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		log.info("manual snapshot date: {}", manualSnapshotDate);

		if (manualSnapshotDate == null || manualSnapshotDate.isEmpty()) {
			snapshotDate = new Date();
			log.info("Using today as snapshotDate.");
		} else {
			log.info("Using manual snapshotDate: {}", manualSnapshotDate);
			try {
				snapshotDate = ymd.parse(manualSnapshotDate);
			} catch (ParseException e) {
				log.warn("Config value for snapshotDate could not be parsed.");
				snapshotDate = new Date();
			}
		}

		if (sonarProjectKeys ==null) {
			throw new ConnectException("No base Component and no componentRoot specified, exiting.");
		}
		if ((sonarInterval == null || sonarInterval.isEmpty())) {
			interval = 3600;
		} else {
			interval = Integer.parseInt(sonarInterval);
		}

	}
	@Override
	public List<SourceRecord> poll() throws InterruptedException {

		List<SourceRecord> records = new ArrayList<>(); 

		if (lastPoll != 0 && System.currentTimeMillis() < (lastPoll + (interval * 1000))) {
				log.info("exit polling {} secs since last poll.",
						( System.currentTimeMillis() - lastPoll ) / 1000 );
				Thread.sleep(1000);
				return records;
		}

		lastPoll = System.currentTimeMillis();
		
		String snapshotDateString = ymd.format(snapshotDate);
		
		int page = 0;
		
		if (sonarProjectKeys != null && !sonarProjectKeys.isEmpty()) {
			SonarIssuesResult iResult;
			do {
				page++;
				iResult = SonarApi.getIssues(sonarToken, sonarProjectKeys, page);
				records.addAll( getSonarIssueRecords(iResult, snapshotDateString) );
			} while ( page*iResult.paging.pageSize < iResult.paging.total );

			page = 0;
			SonarMeasuresResult smr;
			do {
				page++;
				smr = SonarApi.getMeasures(sonarToken, sonarProjectKeys, sonarMetricKeys, page);
				records.addAll(getSonarMeasureRecords(smr, snapshotDateString));
			} while ( page * smr.paging.pageSize < smr.paging.total );
		}
		return records;

	}

	private List<SourceRecord>  getSonarMeasureRecords(SonarMeasuresResult mResult, String snapshotDateString) {
		
		List<SourceRecord> result = new ArrayList<>();
		for (Component c : mResult.components) {
			for (Measure m : c.measures) {
				Struct struct = new Struct( SonarSchema.sonarmeasure );
				struct.put(SonarSchema.FIELD_SONAR_SNAPSHOT_DATE, snapshotDateString);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_ID, mResult.baseComponent.id);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_KEY, mResult.baseComponent.key);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_NAME, mResult.baseComponent.name);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_QUALIFIER, mResult.baseComponent.qualifier);

				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_ID, c.id);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_KEY, c.key);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_NAME, c.name);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_QUALIFIER, c.qualifier);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_PATH, c.path);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_LANGUAGE, c.language);

				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_METRIC, m.metric);
				struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_VALUE, m.value );
				
				if (m.value != null) {
					try {
						float intvalue = Float.parseFloat(m.value);
						struct.put(SonarSchema.FIELD_SONAR_MEASURE_COMPONENT_FLOATVALUE, intvalue );
					} catch ( NumberFormatException nfe ) {
						nfe.printStackTrace();
					}
				}
				Map<String,String> hm = new HashMap<>();
				hm.put("2", "2");
				SourceRecord sr = new SourceRecord(hm, hm, sonarMeasureTopic, SonarSchema.sonarmeasure , struct);
				result.add(sr);
			}
			
		}
		log.info("Found {} metrics", result.size());
		return result;
	}

	

	private List<SourceRecord>  getSonarIssueRecords(SonarIssuesResult iResult, String snapshotDateString) {
		List<SourceRecord> result = new ArrayList<>();
		for (Issue i : iResult.issues) {
			Struct struct = new Struct( SonarSchema.sonarissue );
			struct.put(SonarSchema.FIELD_SONAR_SNAPSHOT_DATE, snapshotDateString);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_RULE, i.rule);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_SEVERITY, i.severity);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_COMPONENT, i.component);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_COMPONENTID, i.componentId);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_PROJECT, i.project);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_LINE, i.line);
			if (i.textRange!= null) {
				struct.put(SonarSchema.FIELD_SONAR_ISSUE_STARTLINE, i.textRange.startLine);
				struct.put(SonarSchema.FIELD_SONAR_ISSUE_STARTOFFSET, i.textRange.startOffset);
				struct.put(SonarSchema.FIELD_SONAR_ISSUE_ENDLINE, i.textRange.endLine);
				struct.put(SonarSchema.FIELD_SONAR_ISSUE_ENDOFFSET, i.textRange.endOffset);
			}
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_STATUS, i.status);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_MESSAGE, i.message);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_EFFORT, i.effort);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_DEBT, i.debt);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_AUTHOR, i.author);
			struct.put(SonarSchema.FIELD_SONAR_ISSUE_CREATIONDATE, i.creationDate);

			Map<String,String> m = new HashMap<>();
			m.put("2", "2");
			
			SourceRecord sr = new SourceRecord(m, m, sonarIssueTopic, SonarSchema.sonarissue , struct);
			result.add(sr);
		}
		log.info("Found {} issues ", result.size());
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
