/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license.
 */

package connect.sonarCloud;

import model.sonarqube.issues.Issue;
import model.sonarqube.issues.SonarCloudIssuesResult;
import model.sonarqube.measures.Component;
import model.sonarqube.measures.Measure;
import model.sonarqube.measures.SonarCloudMeasuresResult;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kafka Connector Task for Sonarqube
 * @author Axel Wickenkamp
 *
 */
public class SonarCloudSourceTask extends SourceTask {

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	static {
		dfZULU.setTimeZone(tzUTC);
	}
	
	private static DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

	private String version = "0.0.1";
	

	private String sonarURL;
	private String sonarUser;
	private String sonarPass;
	private String sonarBaseComponentKey;
	private String sonarMeasureTopic;
	private String sonarMetrics;
	private String sonarProjectKeys;
	private String sonarIssueTopic;
	private String sonarInterval;
	private Integer interval;
	private Date snapshotDate;

	
	// millis of last poll
	private long lastPoll = 0;

	
	private Logger log = Logger.getLogger(SonarCloudSourceTask.class.getName());

	@Override
	public List<SourceRecord> poll() throws InterruptedException {

		List<SourceRecord> records = new ArrayList<>(); 
		
		// log.info("lastPollDelta:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval );
		
		if (lastPoll != 0) {
			if (System.currentTimeMillis() < (lastPoll + (interval * 1000))) {
				log.info("----------------------------------------------------------- exit polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
				Thread.sleep(1000);
				return records;
			}
		} 
		
		lastPoll = System.currentTimeMillis();
		
		String snapshotDateString = ymd.format(snapshotDate);
		
		int page = 0;
		
		if (sonarProjectKeys!= null && !sonarProjectKeys.isEmpty()) {
			SonarCloudIssuesResult iResult;
			do {
				page++;
				iResult = SonarCloudApi.getIssues(sonarURL, sonarUser, sonarPass, sonarProjectKeys, page);
				records.addAll( getSonarIssueRecords(iResult, snapshotDateString) );
			} while ( page*iResult.paging.pageSize < iResult.paging.total );
			
		}
		
		if (sonarBaseComponentKey != null && !sonarBaseComponentKey.isEmpty()) {
			page = 0;
			SonarCloudMeasuresResult smr;
			do {
				page++;
				smr = SonarCloudApi.getMeasures(sonarURL, sonarUser, sonarPass, sonarMetrics, sonarBaseComponentKey, page);
				records.addAll(getSonarMeasureRecords(smr, snapshotDateString));
			} while ( page * smr.paging.pageSize < smr.paging.total );
			
		}

		return records;

	}

	private List<SourceRecord>  getSonarMeasureRecords(SonarCloudMeasuresResult mResult, String snapshotDateString) {
		
		List<SourceRecord> result = new ArrayList<>();
		
		for ( Component c : mResult.components ) {
		
			for ( Measure m : c.measures ) {
			
				Struct struct = new Struct( SonarCloudSchema.sonarmeasure );
				struct.put(SonarCloudSchema.FIELD_SONAR_URL, sonarURL);
				struct.put(SonarCloudSchema.FIELD_SONAR_SNAPSHOT_DATE, snapshotDateString);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_ID, mResult.baseComponent.id);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_KEY, mResult.baseComponent.key);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_NAME, mResult.baseComponent.name);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_BASECOMPONENT_QUALIFIER, mResult.baseComponent.qualifier);
	
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_ID, c.id);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_KEY, c.key);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_NAME, c.name);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_QUALIFIER, c.qualifier);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_PATH, c.path);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_LANGUAGE, c.language);

			
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_METRIC, m.metric);
				struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_VALUE, m.value );
				
				if ( m.value != null ) {
					try {
						float intvalue = Float.parseFloat(m.value);
						struct.put(SonarCloudSchema.FIELD_SONAR_MEASURE_COMPONENT_FLOATVALUE, intvalue );
					} catch ( NumberFormatException nfe ) {
						nfe.printStackTrace();
					}
				}
				Map<String,String> hm = new HashMap<String, String>();
				hm.put("2", "2");
				SourceRecord sr = new SourceRecord(hm, hm, sonarMeasureTopic, SonarCloudSchema.sonarmeasure , struct);

				result.add(sr);
			}
			
		}
		
		log.info("Found " + result.size() + " metrics.");
		
		return result;
	}

	

	private List<SourceRecord>  getSonarIssueRecords(SonarCloudIssuesResult iResult, String snapshotDateString) {
		List<SourceRecord> result = new ArrayList<>();
		for (Issue i : iResult.issues) {
			Struct struct = new Struct( SonarCloudSchema.sonarissue );
			struct.put(SonarCloudSchema.FIELD_SONAR_URL, sonarURL);
			struct.put(SonarCloudSchema.FIELD_SONAR_SNAPSHOT_DATE, snapshotDateString);
			
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_RULE, i.rule);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_SEVERITY, i.severity);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_COMPONENT, i.component);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_COMPONENTID, i.componentId);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_PROJECT, i.project);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_LINE, i.line);
			if (i.textRange!= null) {
				struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_STARTLINE, i.textRange.startLine);
				struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_STARTOFFSET, i.textRange.startOffset);
				struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_ENDLINE, i.textRange.endLine);
				struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_ENDOFFSET, i.textRange.endOffset);
			}
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_STATUS, i.status);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_MESSAGE, i.message);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_EFFORT, i.effort);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_DEBT, i.debt);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_AUTHOR, i.author);
			struct.put(SonarCloudSchema.FIELD_SONAR_ISSUE_CREATIONDATE, i.creationDate);

			Map<String,String> m = new HashMap<String, String>();
			m.put("2", "2");
			
			SourceRecord sr = new SourceRecord(m, m, sonarIssueTopic, SonarCloudSchema.sonarissue , struct);
			result.add(sr);
			
		}
		log.info("Found " + result.size() + " issues.");
		return result;
	}
	
	
	
	@Override
	public void start(Map<String, String> props) {

		log.info("connect-sonarqube: start");
		sonarURL 				= props.get(SonarCloudSourceConfig.SONAR_URL_CONFIG);
		sonarUser 				= props.get(SonarCloudSourceConfig.SONAR_USER_CONFIG);
		sonarPass 				= props.get(SonarCloudSourceConfig.SONAR_PASS_CONFIG);
		
		sonarBaseComponentKey 	= props.get(SonarCloudSourceConfig.SONAR_BCK_CONFIG);
		sonarMeasureTopic 		= props.get(SonarCloudSourceConfig.SONAR_MEASURE_TOPIC_CONFIG);
		sonarMetrics 			= props.get(SonarCloudSourceConfig.SONAR_METRIKKEYS_CONFIG);
		
		sonarProjectKeys 	= props.get(SonarCloudSourceConfig.SONAR_PROJECT_KEYS_CONFIG);
		sonarIssueTopic			= props.get(SonarCloudSourceConfig.SONAR_ISSUE_TOPIC_CONFIG);
		
		sonarInterval = props.get(SonarCloudSourceConfig.SONAR_INTERVAL_SECONDS_CONFIG);
		
		String manualSnapshotDate  = props.get( SonarCloudSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		log.info(props.toString());
		log.info(SonarCloudSourceConfig.SONAR_SNAPSHOTDATE_CONFIG);
		log.info(manualSnapshotDate);

		
		if (manualSnapshotDate==null || manualSnapshotDate.isEmpty()) {
			snapshotDate = new Date();
			log.info("Using today as snapshotDate.");
		} else {
			log.info("Using manual snapshotDate: " + manualSnapshotDate);
			try {
				snapshotDate = ymd.parse(manualSnapshotDate);
			} catch (ParseException e) {
				log.warning("Config value for snapshotDate could not be parsed.");
				snapshotDate = new Date();
			}
		}
			
		
		if (sonarProjectKeys==null && sonarBaseComponentKey==null) {
			throw new ConnectException("No base Component and no componentRoot specified, exiting.");
		}
		if ((sonarInterval == null || sonarInterval.isEmpty())) {
			interval = 3600;
		} else {
			interval = Integer.parseInt(sonarInterval);
		}

	}

	@Override
	public void stop() {
	}

	public String version() {
		return version;
	}

}
