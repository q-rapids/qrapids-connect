package connect.svn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 * Kafka SVN Connector Source Task
 * @author wicken
 *
 */
public class SubversionSourceTask extends SourceTask {
	
	private Long lastSeenRevision=0L;

	private String version = "0.0.2";

	private String repositoryURL;
	private String repositoryPath;
	private String repositoryUser;
	private String repositoryPass;
	private String topic;
	private int interval;
	
	private static final String FIELD_REVISION = "revision";
	private static final String FIELD_MESSAGE = "message";
	private static final String FIELD_AUTHOR = "author";
	private static final String FIELD_DATE = "date";
	private static final String FIELD_FILENAME = "filename";
	private static final String FIELD_NODEKIND = "nodekind";
	private static final String FIELD_ACTION = "action";

	private SVNRepository svnRepo;
	
	private Logger log = Logger.getLogger(SubversionSourceTask.class.getName());
	
	private long lastPoll = 0;
	
	/**
	 * Schema for SVN Log
	 */
	private Schema  schema = SchemaBuilder.struct().name("svn")
				.field(FIELD_REVISION , Schema.INT64_SCHEMA)
				.field(FIELD_MESSAGE, Schema.OPTIONAL_STRING_SCHEMA)
				.field(FIELD_AUTHOR, Schema.STRING_SCHEMA)
				.field(FIELD_DATE, Schema.STRING_SCHEMA)
				.field(FIELD_FILENAME, Schema.STRING_SCHEMA)
				.field(FIELD_NODEKIND, Schema.STRING_SCHEMA)
				.field(FIELD_ACTION, Schema.STRING_SCHEMA);

	
	@Override
	public String version() {
		return version;
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {


		ArrayList<SourceRecord> records = new ArrayList<>();

		HashMap<String, String> sourcePartition = new HashMap<>();
		sourcePartition.put("url", repositoryURL);
		sourcePartition.put("path", repositoryPath);
		
		

		if ( lastPoll != 0 ) {
			if ( System.currentTimeMillis() < ( lastPoll + (interval * 1000) ) ) {
				log.info("----------------------------------------------------------- not polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
				Thread.sleep(1000);
				return records;
			}
		} 
		
		lastPoll = System.currentTimeMillis();

		try {
			svnRepo = getRepository();
			Long latestRevision = svnRepo.getLatestRevision();

			if (latestRevision <= lastSeenRevision ) {
				log.info("kafka-connect-svn: exiting - all data already seen.");
				return records;
			}

			String[] targetPaths = new String[] { repositoryPath };
			ArrayList<SVNLogEntry> entries = new ArrayList<>();

			svnRepo.log(targetPaths, entries, lastSeenRevision + 1, -1, true, false);

			for (SVNLogEntry e : entries) {

				Long revision = e.getRevision();

				Map<String, Long> sourceOffset = Collections.singletonMap(FIELD_REVISION, revision);

				for (SVNLogEntryPath lep : e.getChangedPaths().values()) {
					
					
					TimeZone tz = TimeZone.getTimeZone("UTC");
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
					df.setTimeZone(tz);
					String nowAsISO = df.format( e.getDate() );

					Struct struct = new Struct( schema )
							.put(FIELD_REVISION, e.getRevision())
							.put(FIELD_MESSAGE, e.getMessage())
							.put(FIELD_AUTHOR, e.getAuthor())
							.put(FIELD_DATE, nowAsISO )
							.put(FIELD_FILENAME, lep.getPath())
							.put(FIELD_NODEKIND, lep.getKind().toString())
							.put(FIELD_ACTION, String.valueOf( lep.getType() ) );
					
					SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, topic, schema , struct);

					records.add(sr);
				}

			}
			
			lastSeenRevision = latestRevision;
			
			log.info("POLLED " + records.size() + " SourceRecords.");

			return records;

		} catch (SVNException e) {
			log.warning(e.getMessage());
		}

		
		return records;

	}

	@Override
	public void start(Map<String, String> props) {

		log.info("kafka-connect-svn: start");

		repositoryURL = props.get(SubversionSourceConnector.REPO_URL_CONFIG);
		repositoryPath = props.get(SubversionSourceConnector.REPO_PATH_CONFIG);
		repositoryUser = props.get(SubversionSourceConnector.REPO_USER_CONFIG);
		repositoryPass = props.get(SubversionSourceConnector.REPO_PASS_CONFIG);
		topic = props.get(SubversionSourceConnector.TOPIC_CONFIG);
		
		String intSecs = props.get(SubversionSourceConnector.POLL_INTERVAL_SECONDS_CONFIG);
		
		if ( intSecs != null ) {
			interval = Integer.parseInt(intSecs);
		} else {
			interval = 60;
		}
		
		
		HashMap<String, String> sourcePartition = new HashMap<>();
		sourcePartition.put("url", repositoryURL);
		sourcePartition.put("path", repositoryPath);
		
		
		if ( context != null ) {
			Map<String, Object> offset = context.offsetStorageReader().offset(sourcePartition);
			if (offset != null) {
				Long lastRecordedOffset = (Long) offset.get(FIELD_REVISION);
				if (lastRecordedOffset == null) {
					lastSeenRevision = 0L;
				} else {
					log.info("kafka-connect-svn: OFFSET: STARTING FROM REVISION " + lastRecordedOffset);
					lastSeenRevision = lastRecordedOffset;
				}
			} else {
				lastSeenRevision = 0L;
			}
		}
		

	}

	@Override
	public void stop() {
		// not implemented, necessary?
	}

	private SVNRepository getRepository() throws SVNException {

		System.setProperty("https.protocols", "TLSv1");

		// Initializes the library (it must be done before ever using the library itself)
		setupLibrary();
		SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repositoryURL));

		repository.setAuthenticationManager(
				BasicAuthenticationManager.newInstance(repositoryUser, repositoryPass.toCharArray()));

		return repository;
	}

	/*
	 * Initializes the library to work with a repository via different
	 * protocols.
	 */
	private static void setupLibrary() {

		// use over http:// and https://
		DAVRepositoryFactory.setup();

		// use over svn:// and svn+xxx://
		SVNRepositoryFactoryImpl.setup();

		// use over file:///
		FSRepositoryFactory.setup();

	}

}
