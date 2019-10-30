package connect.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.storage.OffsetStorageReader;

import com.google.gson.Gson;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.Range;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.TestResult;

import model.jenkins.Action;
import model.jenkins.BuildWithDetails;
import model.jenkins.Parameter;
import rest.RESTInvoker;


/**
 * Kafka SVN Connector Source Task
 * @author wicken
 *
 */
public class JenkinsSourceTask extends SourceTask {

	private TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	
	Gson gson = new Gson();

	private String version = "0.0.1";

	private String topic;

	private URI serverURI;
	private String user;
	private String pass;
	
	private JenkinsServer jenkinsServer;
	
	private int interval;
	private int lookback;
	
	private Boolean usePreemptiveAuth=false;
	
	// millis lof last poll
	private long lastPoll = 0;

	private Boolean firstPoll = true;
	
	private Logger log = Logger.getLogger(JenkinsSourceTask.class.getName());
	
	private List<String> joblist = new ArrayList<>();
	private Map<String, JobWithDetails> jobCache = new HashMap<>();
	private Map<String, Integer> jobOffsets = new HashMap<>();
	
	
	class BuildComparator implements Comparator<Build> {

		@Override
		public int compare(Build o1, Build o2) {
			return ((Integer)o1.getNumber()).compareTo(o2.getNumber());
		}
		
	}
	

	@Override
	public List<SourceRecord> poll() throws InterruptedException {

		// collect records
		ArrayList<SourceRecord> records = new ArrayList<>(); 
		
		log.info("lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval );
		
		if ( lastPoll != 0 ) {
			if ( System.currentTimeMillis() < ( lastPoll + (interval * 1000) ) ) {
				log.info("----------------------------------------------------------- not polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
				Thread.sleep(1000);
				return records;
			}
		} 
		
		lastPoll = System.currentTimeMillis();
		
		
		if ( firstPoll ) {
			for ( JobWithDetails jwd : jobCache.values() ) {
				// offset available?
				Integer offset = jobOffsets.get( jwd.getName() );
				
				Range rg;
				if ( offset != null ) {
					if ( offset < jwd.getLastBuild().getNumber() ) {
						rg = Range.build().to( jwd.getLastBuild().getNumber() - offset ).build();
					} else {
						// nothing to do
						continue;
					}
				} else { // no offset, lookback or take it all
					if ( lookback != 0 ) {
						rg = Range.build().from(0).to(lookback);
					} else {
						rg = Range.build().from(0).build();
					}
				}
				
				List<SourceRecord> buildSourceRecords;
				
				if ( usePreemptiveAuth ) {
					buildSourceRecords = getBuildSourceRecordsPreemptive(jenkinsServer, jwd, rg);
				} else {
					buildSourceRecords = getBuildSourceRecords(jenkinsServer, jwd, rg);
				}
				
				records.addAll( buildSourceRecords );

			}
			firstPoll = false;
			
		} else { // all offsets should be present
			for ( JobWithDetails jwd : jobCache.values() ) {
				// update job
				JobWithDetails actual;
				try {
					actual = jenkinsServer.getJob( jwd.getName() );
					int lastBuildNumber = actual.getLastBuild().getNumber();
					Integer offset = jobOffsets.get( jwd.getName() );
					
					Range rg;
					if ( offset < lastBuildNumber ) {
						rg = Range.build().to( lastBuildNumber - offset ).build();
					} else {
						// nothing to do
						continue;
					}
					
					List<SourceRecord> buildSourceRecords;
					
					if ( usePreemptiveAuth ) {
						buildSourceRecords = getBuildSourceRecordsPreemptive(jenkinsServer, jwd, rg);
					} else {
						buildSourceRecords = getBuildSourceRecords(jenkinsServer, jwd, rg);
					}
					
					records.addAll( buildSourceRecords );
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	
		
		return records;

	}
	
	private List<SourceRecord> getBuildSourceRecordsPreemptive(JenkinsServer jenkinsServer, JobWithDetails jwd, Range rg) {

		List<SourceRecord> result = new ArrayList<>();
		
		try {

			List<Build> builds = new ArrayList<>();
			builds.addAll( jwd.getAllBuilds(rg) );
			
			// update offset
			jobOffsets.put( jwd.getName(), builds.get(0).getNumber() );
			
			log.info("Found " + builds.size() + " builds.");
			
			java.util.Collections.sort( builds, new BuildComparator() );
			
			String createdISO   = df.format( new Date() );
			
			for ( Build b : builds ) {
				log.info("-------------------------------- found build " + b.getNumber());
				
				// RESTInvoker ri = new RESTInvoker(serverURI + "/job/build_ongoing_compile/" + b.getNumber() + "/api/json", user , pass);
				RESTInvoker ri = new RESTInvoker(serverURI + "/job/" + jwd.getName() + "/" + b.getNumber() + "/api/json", user , pass);
				BuildWithDetails bwd = gson.fromJson(ri.getDataFromServer(""), BuildWithDetails.class);
				
				Date tsdate = new Date(bwd.timestamp);
				String timestampISO = df.format( tsdate );
				
				Struct struct = new Struct( JenkinsSchema.buildSchema )
						.put( JenkinsSchema.FIELD_CREATED, createdISO )
						.put( JenkinsSchema.FIELD_JENKINS_URL, serverURI.toString() )
						.put( JenkinsSchema.FIELD_JOB_NAME, jwd.getName() )
						.put( JenkinsSchema.FIELD_JOB_URL, jwd.getUrl() )
						.put( JenkinsSchema.FIELD_JOB_CLASS, jwd.get_class())
						.put( JenkinsSchema.FIELD_DISPLAY_NAME, jwd.getDisplayName())
						.put( JenkinsSchema.FIELD_LAST_BUILD, jwd.getLastBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_COMPLETED_BUILD, jwd.getLastCompletedBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_FAILED_BUILD, jwd.getLastFailedBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_STABLE_BUILD, jwd.getLastStableBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_SUCCESSFUL_BUILD, jwd.getLastSuccessfulBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_UNSTABLE_BUILD, jwd.getLastUnstableBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_UNSUCCESSFUL_BUILD, jwd.getLastUnsuccessfulBuild().getNumber() )
						.put( JenkinsSchema.FIELD_BUILD_CLASS, b.get_class() )
						.put( JenkinsSchema.FIELD_BUILD_NUMBER, b.getNumber() )
						.put( JenkinsSchema.FIELD_BUILD_URL, b.getUrl() )
						.put( JenkinsSchema.FIELD_BUILD_DESCRIPTION, bwd.description )
						.put( JenkinsSchema.FIELD_BUILD_DURATION, bwd.duration )
						.put( JenkinsSchema.FIELD_BUILD_ESTIMATED_DURATION, bwd.estimatedDuration )
						.put( JenkinsSchema.FIELD_BUILD_RESULT, bwd.result )
						.put( JenkinsSchema.FIELD_TIMESTAMP, timestampISO );
				
				
				Map<String,Object> paramMap = new HashMap<>();
				
				log.info("actions field: " + bwd.actions);
				
				
				for ( Action a : bwd.actions ) {
					if ( a._class != null && a._class.equals("hudson.model.ParametersAction") ) {
						for ( Parameter p : a.parameters ) {
							paramMap.put(p.name, p.value);
						}
					}
				}

				struct.put(JenkinsSchema.FIELD_PARAM_BRANCH, paramMap.get("BRANCH"));
				struct.put(JenkinsSchema.FIELD_PARAM_SUBBRANCH, paramMap.get("SUBBRANCH"));
				struct.put(JenkinsSchema.FIELD_PARAM_PRODUCT, paramMap.get("PRODUCT"));
				struct.put(JenkinsSchema.FIELD_PARAM_REVISION, paramMap.get("REVISION"));
				struct.put(JenkinsSchema.FIELD_PARAM_UUID, paramMap.get("UUID"));
				struct.put(JenkinsSchema.FIELD_PARAM_RESULT, paramMap.get("RESULT"));
				
				Map<String,String> sourcePartition = new HashMap<>();
				sourcePartition.put( "serverURI", serverURI.toString() );
				sourcePartition.put( "jobURL", jwd.getUrl() );
				
				Map<String,Integer> sourceOffset = new HashMap<>();
				sourceOffset.put( "buildNumber", b.getNumber() );

				
				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, JenkinsSchema.buildSchema , struct);
				result.add(sr);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("Found " + result.size() + " builds.");
		
		return result;
	}

	private List<SourceRecord> getBuildSourceRecords(JenkinsServer jenkinsServer, JobWithDetails jwd, Range rg) {
		
		List<SourceRecord> result = new ArrayList<>();
		
		try {

			List<Build> builds = new ArrayList<>();
			builds.addAll( jwd.getAllBuilds(rg) );
			
			// update offset
			jobOffsets.put( jwd.getName(), builds.get(0).getNumber() );
			
			log.info("Found " + builds.size() + " builds.");
			
			java.util.Collections.sort( builds, new BuildComparator() );
			
			for ( Build b : builds ) {
				
				com.offbytwo.jenkins.model.BuildWithDetails bwd = b.details();
				
				Date tsdate = new Date(bwd.getTimestamp());
				String timestampISO = df.format( tsdate );
				String createdISO   = df.format( new Date() );
				
				Struct struct = new Struct( JenkinsSchema.buildSchema )
						.put( JenkinsSchema.FIELD_CREATED, createdISO )
						.put( JenkinsSchema.FIELD_JENKINS_URL, serverURI.toString() )
						.put( JenkinsSchema.FIELD_JOB_NAME, jwd.getName() )
						.put( JenkinsSchema.FIELD_JOB_URL, jwd.getUrl() )
						.put( JenkinsSchema.FIELD_JOB_CLASS, jwd.get_class())
						.put( JenkinsSchema.FIELD_DISPLAY_NAME, jwd.getDisplayName())
						.put( JenkinsSchema.FIELD_LAST_BUILD, jwd.getLastBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_COMPLETED_BUILD, jwd.getLastCompletedBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_FAILED_BUILD, jwd.getLastFailedBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_STABLE_BUILD, jwd.getLastStableBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_SUCCESSFUL_BUILD, jwd.getLastSuccessfulBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_UNSTABLE_BUILD, jwd.getLastUnstableBuild().getNumber() )
						.put( JenkinsSchema.FIELD_LAST_UNSUCCESSFUL_BUILD, jwd.getLastUnsuccessfulBuild().getNumber() )
						.put( JenkinsSchema.FIELD_BUILD_CLASS, b.get_class() )
						.put( JenkinsSchema.FIELD_BUILD_NUMBER, b.getNumber() )
						.put( JenkinsSchema.FIELD_BUILD_URL, b.getUrl() )
						.put( JenkinsSchema.FIELD_BUILD_DESCRIPTION, bwd.getDescription() )
						.put( JenkinsSchema.FIELD_BUILD_DURATION, bwd.getDuration() )
						.put( JenkinsSchema.FIELD_BUILD_ESTIMATED_DURATION, bwd.getEstimatedDuration() )
						.put( JenkinsSchema.FIELD_BUILD_RESULT, bwd.getResult().toString() )
						.put( JenkinsSchema.FIELD_TIMESTAMP, timestampISO );


				try {
					TestResult tr = bwd.getTestResult();
					if ( tr != null ) {
						struct.put( JenkinsSchema.FIELD_TEST_FAIL, (long) tr.getFailCount() );
						struct.put( JenkinsSchema.FIELD_TEST_PASS, (long) tr.getPassCount() );
						struct.put( JenkinsSchema.FIELD_TEST_SKIP, (long) tr.getSkipCount() );
						struct.put( JenkinsSchema.FIELD_TEST_DURATION,  tr.getDuration() );
					}
				} catch ( Exception e ) {
					log.info("no test data found for " + jwd.getName() + "/" + b.getNumber());
				}
				
				Map<String,String> sourcePartition = new HashMap<>();
				sourcePartition.put( "serverURI", serverURI.toString() );
				sourcePartition.put( "jobURL", jwd.getUrl() );
				
				Map<String,Integer> sourceOffset = new HashMap<>();
				sourceOffset.put( "buildNumber", b.getNumber() );

				
				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, JenkinsSchema.buildSchema , struct);
				result.add(sr);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info("Found " + result.size() + " builds.");
		
		return result;
	}

	@Override
	public void start(Map<String, String> props) {
		
		df.setTimeZone(tz);

		log.info("kafka-connect-jenkins: start");

		try {
			
			serverURI = new URI( props.get(JenkinsSourceConfig.JENKINS_URL_CONFIG) );
			user = props.get( JenkinsSourceConfig.JENKINS_USER_CONFIG );
			pass = props.get( JenkinsSourceConfig.JENKINS_PASS_CONFIG );
			topic = props.get( JenkinsSourceConfig.JENKINS_TOPIC_CONFIG );
			
			usePreemptiveAuth = Boolean.parseBoolean( props.get( JenkinsSourceConfig.JENKINS_AUTH_CONFIG ) );

			if ( (user == null || user.isEmpty()) && ( pass==null || pass.isEmpty() ) ) {
				jenkinsServer = new JenkinsServer( serverURI ); 
			} else {
				JenkinsHttpClient jhttpc = new JenkinsHttpClient(serverURI, user, pass);
				log.info( jhttpc.getJenkinsVersion() );
				jenkinsServer = new JenkinsServer( jhttpc );
			}
			
			String jobs = props.get(JenkinsSourceConfig.JENKINS_JOBS_CONFIG);
			if ( !(jobs == null || jobs.isEmpty()) ) {
				joblist = Arrays.asList( jobs.split(",") );
				for ( String jobName : joblist ) {
					JobWithDetails j = jenkinsServer.getJob(jobName);
					if ( j == null ) {
						log.warning("Job " + jobName + " not found. ignored.");
					} else {
						jobCache.put( jobName, j );
					}
				}
			} else {
				Map<String,Job> jobmap  = jenkinsServer.getJobs();
				for ( Job j : jobmap.values() ) {
					jobCache.put( j.getName(), jenkinsServer.getJob(j.getName()));
				}
			}
			
			String i = props.get(JenkinsSourceConfig.JENKINS_INTERVAL_SECONDS_CONFIG);
			if ( (i == null || i.isEmpty()) ) {
				interval = 60;
			} else {
				interval = Integer.parseInt(i);
			}
			
			String lb = props.get(JenkinsSourceConfig.JENKINS_LOOKBACK_CONFIG);
			if ( (lb == null || lb.isEmpty()) ) { 
				lookback = 0;
			} else {
				lookback = Integer.parseInt(lb);
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new ConnectException("Error decoding URI.");
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}

		// offsets
		if ( context != null ) {
			OffsetStorageReader osr = context.offsetStorageReader();
			for ( String jobName : jobCache.keySet() ) {
				
				Map<String,String> sourcePartition = new HashMap<>();
				sourcePartition.put( "serverURI", serverURI.toString() );
				
				log.info(jobCache.toString());
				log.info(jobCache.get(jobName).toString());
				log.info(jobCache.get(jobName).getUrl());
				
				sourcePartition.put( "jobURL", jobCache.get(jobName).getUrl() );
				
				Map<String,Object> offset = osr.offset(sourcePartition);
				if ( offset != null ) {
					
					Integer buildNumber = (int) (long) offset.get("buildNumber");
					
					log.info("-------------------------------------- Job: " + jobName + " offset:" + offset.get("buildNumber") );
					
					jobOffsets.put(jobName, buildNumber);
				}
			}
		}
	}

	@Override
	public void stop() {
		// not implemented, necessary?
	}



	public String version() {
		return version;
	}

}
