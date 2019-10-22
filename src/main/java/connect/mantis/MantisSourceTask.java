package connect.mantis;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class MantisSourceTask extends SourceTask {

	private String version = "0.1.0";

	private String mantisURL;
	private String mantisUser;
	private String mantisPass;
	private String mantisProject;
	private String issueTopic;
	private String updateTopic;
	private String statTopic;

	private long lastPoll = 0;

	private int interval;

	private static final String FIELD_ISSUE_ID = "issueId";
	private static final String FIELD_ISSUE_TYPE = "issueType";
	private static final String FIELD_CREATED = "created";
	private static final String FIELD_LAST_UPDATE = "lastUpdated";
	private static final String FIELD_OPEN_DURATION = "openDuration";
	private static final String FIELD_ISSUE_DESCRIPTION = "issueDescription";
	private static final String FIELD_BUILD_DETAILS = "buildDetails";
	private static final String FIELD_ISSUE_STATUS = "issueStatus";
	private static final String FIELD_TEST_FEEDBACK = "testFeedback";
	private static final String FIELD_CUSTOMER = "customer";
	private static final String FIELD_MANTIS_URL = "mantisURL";
	private static final String FIELD_PROJECT = "project";
	private static final String FIELD_PRIORITY = "priority";
	private static final String FIELD_SEVERITY = "severity";
	private static final String FIELD_REPRODUCTIBILITY = "reproducibility";
	private static final String FIELD_OS = "os";
	private static final String FIELD_PLATFORM = "platform";
	
	private static final String FIELD_POST_DATE = "postdate";
	private static final String FIELD_BUILD = "build";
	private static final String FIELD_TOTAL_OPEN = "total_open";
	private static final String FIELD_TOTAL_CLOSED = "total_closed";
	private static final String FIELD_LOW_SEVERITY_OPEN = "low_severity_open";
	private static final String FIELD_LOW_SEVERITY_CLOSED= "low_severityclosed";
	private static final String FIELD_HIGH_SEVERITY_OPEN = "high_severity_open";
	private static final String FIELD_HIGH_SEVERITY_CLOSED= "high_severity_closed";


	private Schema mantisSchema = SchemaBuilder.struct().name("mantis")
			.field(FIELD_ISSUE_ID, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_TYPE, Schema.STRING_SCHEMA)
			.field(FIELD_CREATED, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_DESCRIPTION, Schema.STRING_SCHEMA)
			.field(FIELD_BUILD_DETAILS, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_STATUS, Schema.STRING_SCHEMA)
			.field(FIELD_CUSTOMER, Schema.STRING_SCHEMA)
			.field(FIELD_MANTIS_URL, Schema.STRING_SCHEMA)
			.field(FIELD_PROJECT, Schema.STRING_SCHEMA)
			.field(FIELD_PRIORITY, Schema.STRING_SCHEMA)
			.field(FIELD_SEVERITY, Schema.STRING_SCHEMA)
			.field(FIELD_REPRODUCTIBILITY, Schema.STRING_SCHEMA)
			.field(FIELD_OS, Schema.STRING_SCHEMA)
			.field(FIELD_PLATFORM, Schema.STRING_SCHEMA);

	private Schema mantisUpdateSchema = SchemaBuilder.struct().name("mantis.update")
			.field(FIELD_ISSUE_ID, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_TYPE, Schema.STRING_SCHEMA)
			.field(FIELD_CREATED, Schema.STRING_SCHEMA)
			.field(FIELD_LAST_UPDATE, Schema.STRING_SCHEMA)
			.field(FIELD_OPEN_DURATION, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_DESCRIPTION, Schema.STRING_SCHEMA)
			.field(FIELD_BUILD_DETAILS, Schema.STRING_SCHEMA)
			.field(FIELD_ISSUE_STATUS, Schema.STRING_SCHEMA)
			.field(FIELD_TEST_FEEDBACK, Schema.STRING_SCHEMA)
			.field(FIELD_CUSTOMER, Schema.STRING_SCHEMA)
			.field(FIELD_MANTIS_URL, Schema.STRING_SCHEMA)
			.field(FIELD_PROJECT, Schema.STRING_SCHEMA)
			.field(FIELD_PRIORITY, Schema.STRING_SCHEMA)
			.field(FIELD_SEVERITY, Schema.STRING_SCHEMA)
			.field(FIELD_REPRODUCTIBILITY, Schema.STRING_SCHEMA)
			.field(FIELD_OS, Schema.STRING_SCHEMA)
			.field(FIELD_PLATFORM, Schema.STRING_SCHEMA);
	

	private Schema mantisStatSchema = SchemaBuilder.struct().name("mantis.stat")
			.field(FIELD_POST_DATE, Schema.STRING_SCHEMA)
			.field(FIELD_PROJECT, Schema.STRING_SCHEMA)		
			.field(FIELD_BUILD, Schema.STRING_SCHEMA)
			.field(FIELD_TOTAL_OPEN, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_TOTAL_CLOSED, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LOW_SEVERITY_OPEN, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_LOW_SEVERITY_CLOSED, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_HIGH_SEVERITY_OPEN, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_HIGH_SEVERITY_CLOSED, Schema.OPTIONAL_INT32_SCHEMA);
	

	private Logger log = Logger.getLogger(MantisSourceTask.class.getName());

	private Date lastExecution;

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	
	static {
		dfZULU.setTimeZone(tzUTC);
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {

		ArrayList<SourceRecord> records = new ArrayList<>();

		if (lastPoll != 0) {
			if (System.currentTimeMillis() < (lastPoll + (interval * 1000))) {
				log.info("----------------------------------------------------------- not polling, "
						+ (System.currentTimeMillis() - lastPoll) / 1000 + " secs since last poll.");
				Thread.sleep(1000);
				return records;
			}
		}

		lastPoll = System.currentTimeMillis();

		HashMap<String, String> sourcePartition = new HashMap<>();
		sourcePartition.put("url", mantisURL);
		sourcePartition.put("project", mantisProject);

		try {

			Class.forName("com.mysql.jdbc.Driver");

			try (Connection connect = DriverManager.getConnection(mantisURL + "?" + "user=" + mantisUser + "&password=" + mantisPass + "&serverTimezone=CET")) {
				records.addAll(collectNewIssue(sourcePartition, connect));
				records.addAll(collectIssueUpdates(sourcePartition, connect));
				records.addAll(collectStats(sourcePartition, connect));
				
				this.lastExecution = new Date();
			}
		} catch (Exception e) {
			log.info("Error in kafka-connect-mantis pool: " + e.getMessage() + " " + getStackTrace(e));
		}
		return records;
	}

	private List<SourceRecord>  collectStats(HashMap<String, String> sourcePartition,Connection connect) throws SQLException{
		List<SourceRecord> records = new ArrayList<>();
		
		
		
		try (Statement statement = connect.createStatement()) {

			// Select Mantis issues of a specific Project after the last execution of the collector
			ResultSet newSet = statement.executeQuery("select * from mantis_bug_table b INNER JOIN mantis_project_table p ON b.project_id=p.id  INNER JOIN mantis_custom_field_string_table cf ON cf.bug_id=b.id INNER JOIN mantis_custom_field_table cft ON cft.id=cf.field_id where cft.name='Customer' and p.name = '" + mantisProject + "' group by b.id");

			Map<String, StatData>  buildMap = new HashMap<>();
			while (newSet.next()) {	
				String build = newSet.getString("version");			
				if(build == null) {
					build ="undefined";
				}
				
				StatData  stat = buildMap.get(build);
				if(stat == null) {
					stat = new StatData(notNull(newSet.getString("name")), build);
					buildMap.put(build, stat);
				}			
				if(newSet.getInt("status") < 80 ) {
					// Opened Issue
					stat.addTotal_open();
					
					if(newSet.getInt("severity") < 60) {
						stat.addLow_severity_open();
					}else {
						stat.addHigh_severity_open();
					}
				}else {
					// Closed Issue
					stat.addTotal_closed();		
					if(newSet.getInt("severity") < 60) {
						stat.addLow_severityclosed();
					}else {
						stat.addHigh_severity_closed();
					}
				}
			}
			
			
			Map<String, String> sourceOffset = new HashMap<>();
			sourceOffset.put("lastExecution", dfZULU.format(new Date()));
			
			for(StatData stat : buildMap.values()) {
				Struct struct = new Struct(mantisStatSchema)						
						.put(FIELD_POST_DATE, dfZULU.format(new Date()))
						.put(FIELD_PROJECT,stat.getProject())
						.put(FIELD_BUILD, stat.getBuild())
						.put(FIELD_TOTAL_OPEN,stat.getTotal_open())
						.put(FIELD_TOTAL_CLOSED, stat.getTotal_closed())
						.put(FIELD_LOW_SEVERITY_OPEN,stat.getLow_severity_open())
						.put(FIELD_LOW_SEVERITY_CLOSED, stat.getLow_severityclosed())
						.put(FIELD_HIGH_SEVERITY_OPEN, stat.getHigh_severity_open())
						.put(FIELD_HIGH_SEVERITY_CLOSED, stat.getHigh_severity_closed());
																		
				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.statTopic, mantisStatSchema, struct);
				records.add(sr);
																						
			}

			
			log.info("Found " + records.size() + " build status.");
		}
		return records;
	}

	/**
	 * Collect issue and add it in an index. Each issues will be present 1 time in the index
	 * @param sourcePartition 
	 * @param connect SQL Commection 
	 * @return SourceRecord
	 * @throws SQLException
	 */
	private List<SourceRecord> collectNewIssue(HashMap<String, String> sourcePartition,Connection connect) throws SQLException {
		
		List<SourceRecord> records = new ArrayList<>();
		try (Statement statement = connect.createStatement()) {

			// Select Mantis issues of a specific Project after the last execution of the collector
			ResultSet newSet = statement.executeQuery("select * from mantis_bug_table b INNER JOIN mantis_project_table p ON b.project_id=p.id  INNER JOIN mantis_custom_field_string_table cf ON cf.bug_id=b.id INNER JOIN mantis_custom_field_table cft ON cft.id=cf.field_id where cft.name='Customer' and p.name = '" + mantisProject + "' and b.date_submitted > " + this.lastExecution.getTime() / 1000  + " group by b.id");

			//b.id,b.status,b.date_submitted,b.summary,b
			
			Map<String, String> sourceOffset = new HashMap<>();
			sourceOffset.put("lastExecution", dfZULU.format(new Date()));

			while (newSet.next()) {

				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no
																			   // Timezone offset
				df.setTimeZone(tz);

				String submitedAsIso = df.format(new Date(newSet.getInt("date_submitted") * 1000L));

				Struct struct = new Struct(mantisSchema)
						.put(FIELD_ISSUE_ID, notNull(newSet.getString("id")))
						.put(FIELD_ISSUE_TYPE, getType(newSet.getInt("status")))
						.put(FIELD_CREATED, submitedAsIso)
						.put(FIELD_ISSUE_DESCRIPTION, notNull(newSet.getString("summary")))
						.put(FIELD_BUILD_DETAILS, notNull(newSet.getString("version")))
						.put(FIELD_ISSUE_STATUS, getStatus(newSet.getInt("status")))
						.put(FIELD_CUSTOMER, notNull(newSet.getString("value")))
						.put(FIELD_MANTIS_URL, mantisURL)
						.put(FIELD_PROJECT, notNull(newSet.getString("name")))
						.put(FIELD_PRIORITY, getPriority(newSet.getInt("priority")))
						.put(FIELD_SEVERITY, getSeverity(newSet.getInt("severity")))
						.put(FIELD_REPRODUCTIBILITY, getReproductibility(newSet.getInt("reproducibility")))
						.put(FIELD_OS, notNull(newSet.getString("os")))
						.put(FIELD_PLATFORM, notNull(newSet.getString("platform")));
		
				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, issueTopic, mantisSchema, struct);
				records.add(sr);
			}
			
			log.info("Found " + records.size() + " new issues.");
		}
		return records;
	}

	/**
	 * Collect data related to all update on the mantis issues
	 * 
	 * @param sourcePartition
	 * @param connect
	 * @return
	 */
	private List<SourceRecord> collectIssueUpdates(HashMap<String, String> sourcePartition, Connection connect)
			throws SQLException {
		List<SourceRecord> records = new ArrayList<>();
		try (Statement statement = connect.createStatement()) {

			// Select Mantis issues of a specific Project updated after the last execution						
			ResultSet newSet = statement.executeQuery("select b.*,p.*,cf.*,bnt.note from mantis_bug_table b INNER JOIN mantis_project_table p ON b.project_id=p.id  INNER JOIN mantis_custom_field_string_table cf ON cf.bug_id=b.id INNER JOIN mantis_custom_field_table cft ON cft.id=cf.field_id LEFT OUTER JOIN mantis_bugnote_table bn ON bn.bug_id=b.id LEFT OUTER JOIN mantis_bugnote_text_table bnt ON bnt.id=bn.bugnote_text_id  where cft.name='Customer' and p.name = '" + mantisProject + "' and b.last_updated > " + this.lastExecution.getTime() / 1000 + " group by b.id");
			

			while (newSet.next()) {

				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no
																				// Timezone offset
				df.setTimeZone(tz);

				String submitedAsIso = df.format(new Date(newSet.getInt("date_submitted") * 1000L));
				String updateddAsIso = df.format(new Date(newSet.getInt("last_updated") * 1000L));
				
				String openDuration = String.valueOf((new Date(newSet.getInt("last_updated") * 1000L).getTime() - new Date(newSet.getInt("date_submitted") * 1000L).getTime() )/ 86400000);

				Map<String, String> sourceOffset = new HashMap<>();
				sourceOffset.put("lastExecution", dfZULU.format(new Date()));
				
				Struct struct = new Struct(mantisUpdateSchema)
						.put(FIELD_ISSUE_ID, notNull(newSet.getString("id")))
						.put(FIELD_ISSUE_TYPE, getType(newSet.getInt("status")))
						.put(FIELD_CREATED, submitedAsIso)
						.put(FIELD_LAST_UPDATE, updateddAsIso)
						.put(FIELD_OPEN_DURATION, notNull(openDuration))
						.put(FIELD_ISSUE_DESCRIPTION, notNull(newSet.getString("summary")))
						.put(FIELD_BUILD_DETAILS, notNull(newSet.getString("version")))
						.put(FIELD_ISSUE_STATUS, getStatus(newSet.getInt("status")))
						.put(FIELD_TEST_FEEDBACK,notNull(newSet.getString("note")))
						.put(FIELD_CUSTOMER, notNull(newSet.getString("value")))
						.put(FIELD_MANTIS_URL, mantisURL)
						.put(FIELD_PROJECT, notNull(newSet.getString("name")))
						.put(FIELD_PRIORITY, getPriority(newSet.getInt("priority")))
						.put(FIELD_SEVERITY, getSeverity(newSet.getInt("severity")))
						.put(FIELD_REPRODUCTIBILITY, getReproductibility(newSet.getInt("reproducibility")))
						.put(FIELD_OS, notNull(newSet.getString("os")))
						.put(FIELD_PLATFORM, notNull(newSet.getString("platform")));		
	
				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, updateTopic, mantisUpdateSchema, struct);
				records.add(sr);
			}

			log.info("Found " + records.size() + " issues updated.");
		}
		return records;
	}

	private String notNull(String value) {
		if(value == null)
			return "";
		return value;
	}

	@Override
	public void start(Map<String, String> props) {
		log.info("kafka-connect-mantis: start");

		mantisURL = props.get(MantisSourceConnector.MANTIS_DATABASE_URL_CONFIG);
		mantisUser = props.get(MantisSourceConnector.MANTIS_DATABASE_USER_CONFIG);
		mantisPass = props.get(MantisSourceConnector.MANTIS_DATABASE_PASS_CONFIG);
		mantisProject = props.get(MantisSourceConnector.MANTIS_PROJECT_CONFIG);
		issueTopic = props.get(MantisSourceConnector.NEW_ISSUE_TOPIC_CONFIG);
		updateTopic = props.get(MantisSourceConnector.UPDATED_ISSUE_TOPIC_CONFIG);
		statTopic = props.get(MantisSourceConnector.STAT_TOPIC_CONFIG);
		

		String intSecs = props.get(MantisSourceConnector.POLL_INTERVAL_SECONDS_CONFIG);

		if (intSecs != null) {
			interval = Integer.parseInt(intSecs);
		} else {
			interval = 60;
		}

		HashMap<String, String> sourcePartition = new HashMap<>();
		sourcePartition.put("url", mantisURL);
		sourcePartition.put("project", mantisProject);

		if (context != null) {
			Map<String, Object> offset = context.offsetStorageReader().offset(sourcePartition);
			if (offset != null) {
				try {
					this.lastExecution= dfZULU.parse((String) offset.get("lastExecution"));
					log.info("--------------------------" + "found offset: lastUpdate=" + lastExecution);
				} catch (ParseException e) {
					log.info("Error in kafka-connect-mantis start: " + e.getMessage());
				}
				
			
			}
		}

		if (this.lastExecution == null) {
			this.lastExecution = new Date(0);
		}
	}

	@Override
	public void stop() {
		// NA
	}

	private static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	/**
	 * Transform Mantis status code into understable value
	 * 
	 * @param value
	 * @return
	 */
	private String getStatus(int value) {
		switch (value) {
		case 10:
			return "new";
		case 15:
			return "reviewed";
		case 20:
			return "feedback";
		case 25:
			return "in work";
		case 30:
			return "acknowledged";
		case 40:
			return "confirmed";
		case 50:
			return "assigned";
		case 55:
			return "usability";
		case 60:
			return "verify fail";
		case 70:
			return "verify wait";
		case 80:
			return "resolved";
		case 90:
			return "closed";
		}
		return "new";
	}

	/**
	 * Transform Mantis severity code into understable value
	 * 
	 * @param value
	 * @return
	 */
	private String getSeverity(int value) {
		switch (value) {
		case 10:
			return "feature";
		case 20:
			return "trivial";
		case 30:
			return "text";
		case 40:
			return "tweak";
		case 50:
			return "minor";
		case 55:
			return "usability";
		case 60:
			return "major";
		case 70:
			return "crash";
		case 80:
			return "block";

		}
		return "feature";
	}

	private String getType(int value) {
		switch (value) {
		case 10:
			return "feature";
		default:
			return "bug";
		}
	}

	/**
	 * Transform Mantis reprodictibility code into understable value
	 * 
	 * @param value
	 * @return
	 */
	private String getReproductibility(int value) {
		switch (value) {
		case 10:
			return "always";
		case 30:
			return "sometimes";
		case 50:
			return "random";
		case 70:
			return "have not tried";
		case 90:
			return "unable";
		}
		return "N/A";
	}

	/**
	 * Transform Mantis priority code into understable value
	 * 
	 * @param value
	 * @return
	 */
	private String getPriority(int value) {
		switch (value) {
		case 10:
			return "low";
		case 20:
			return "normal";
		case 30:
			return "high";
		case 40:
			return "urgent";
		}
		return "imediat";
	}

}
