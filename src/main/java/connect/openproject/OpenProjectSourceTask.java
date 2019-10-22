package connect.openproject;

import java.io.PrintWriter;
import java.io.StringWriter;
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

import model.openproject.OPElement;
import model.openproject.OPResult;
import model.openproject.OPWorkPackage;

public class OpenProjectSourceTask extends SourceTask {

	private String version = "0.1.0";

	private String openprojectURL;
	private String openprojectApiKey;
	private String openprojectProject;
	private String openprojectTopic;
	private String statTopic;

	private long lastPoll = 0;

	private int interval;

	private static final String OPENPROJECT_API_USER = "apikey";

	private static final String FIELD_ISSUE_ID = "issueid";
	private static final String FIELD_SUMMARY = "summary";
	private static final String FIELD_DESCRIPTION = "description";
	private static final String FIELD_ISSUE_TYPE = "issuetype";
	private static final String FIELD_OPENPROJECT_URL = "openProjectUrl";
	private static final String FIELD_PROJECT_ID = "projectId";
	private static final String FIELD_PROJECT_NAME = "projectName";
	private static final String FIELD_VERSION = "version";
	private static final String FIELD_PARENT_ID = "parentId";
	private static final String FIELD_PARENT_SUMMARY = "parentSummary";
	private static final String FIELD_CREATOR = "creator";
	private static final String FIELD_RESPONSIBLE = "responsible";
	private static final String FIELD_ASSIGNEE = "assignee";
	private static final String FIELD_CREATED = "created";
	private static final String FIELD_UPDATED = "updated";
	private static final String FIELD_START_DATE = "startdate";
	private static final String FIELD_DUE_DATE = "duedate";
	private static final String FIELD_TIME_ESTIMATE = "timeestimate";
	private static final String FIELD_TIME_SPENT = "timespent";
	private static final String FIELD_REMAINING_TIME = "remainingTime";
	private static final String FIELD_PERCENTAGE_DONE = "percentageDone";
	private static final String FIELD_PRIORITY = "priority";
	private static final String FIELD_STATUS = "status";
	private static final String FIELD_ONTIME = "ontime";
	
	private static final String POSTDATE = "postdate";
	private static final String PROJECT_PROGRESS = "project_progress";
	private static final String SPECIFICATION_PROGRESS="specification_progress";
	private static final String TASK_PROGRESS="task_progress";
	private static final String OPEN_TASK = "open_task";
	private static final String CLOSED_TASK = "closed_task";
	private static final String OPEN_SPECIFICATION = "open_specification";
	private static final String CLOSED_SPECIFICATION = "closed_specification";	
	private static final String OPEN_FEATURE = "open_features";
	private static final String CLOSED_FEATURE = "closed_featues";
	private static final String QUALITY_REQUIREMENT = "qualityrequirement";
	private static final String OPEN_QUALITY_REQUIREMENT = "open_qualityrequirement";
	private static final String REJECTED_QUALITY_REQUIREMENT= "rejected_qualityrequirement";
	private static final String CLOSED_QUALITY_REQUIREMENT = "closed_qualityrequirement";
	private static final String DERIVED_QUALITY_REQUIREMENT = "derived_qualityrequirement";
	
	


	private Schema schema = SchemaBuilder.struct().name("openproject")
			.field(FIELD_ISSUE_ID, Schema.OPTIONAL_INT32_SCHEMA).field(FIELD_SUMMARY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_DESCRIPTION, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ISSUE_TYPE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_OPENPROJECT_URL, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_PROJECT_ID, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_PROJECT_NAME, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_VERSION, Schema.OPTIONAL_STRING_SCHEMA).field(FIELD_PARENT_ID, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_PARENT_SUMMARY, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_CREATOR, Schema.OPTIONAL_STRING_SCHEMA).field(FIELD_RESPONSIBLE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ASSIGNEE, Schema.OPTIONAL_STRING_SCHEMA).field(FIELD_CREATED, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_UPDATED, Schema.OPTIONAL_STRING_SCHEMA).field(FIELD_START_DATE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_DUE_DATE, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_TIME_ESTIMATE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_TIME_SPENT, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_REMAINING_TIME, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_PERCENTAGE_DONE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(FIELD_PRIORITY, Schema.OPTIONAL_STRING_SCHEMA).field(FIELD_STATUS, Schema.OPTIONAL_STRING_SCHEMA)
			.field(FIELD_ONTIME, Schema.OPTIONAL_STRING_SCHEMA);
	
	
	private Schema schemastat = SchemaBuilder.struct().name("openproject.stat")
			.field(POSTDATE, Schema.STRING_SCHEMA)
			.field(FIELD_PROJECT_NAME, Schema.STRING_SCHEMA)		
			.field(PROJECT_PROGRESS, Schema.OPTIONAL_FLOAT64_SCHEMA)
			.field(SPECIFICATION_PROGRESS, Schema.OPTIONAL_FLOAT64_SCHEMA)
			.field(TASK_PROGRESS, Schema.OPTIONAL_FLOAT64_SCHEMA)
			.field(OPEN_TASK, Schema.OPTIONAL_INT32_SCHEMA)
			.field(CLOSED_TASK, Schema.OPTIONAL_INT32_SCHEMA)
			.field(OPEN_SPECIFICATION, Schema.OPTIONAL_INT32_SCHEMA)
			.field(CLOSED_SPECIFICATION, Schema.OPTIONAL_INT32_SCHEMA)
			.field(OPEN_FEATURE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(CLOSED_FEATURE, Schema.OPTIONAL_INT32_SCHEMA)
			.field(QUALITY_REQUIREMENT, Schema.OPTIONAL_INT32_SCHEMA)	
			.field(OPEN_QUALITY_REQUIREMENT, Schema.OPTIONAL_INT32_SCHEMA)
			.field(REJECTED_QUALITY_REQUIREMENT, Schema.OPTIONAL_INT32_SCHEMA)			
			.field(CLOSED_QUALITY_REQUIREMENT, Schema.OPTIONAL_INT32_SCHEMA)
	        .field(DERIVED_QUALITY_REQUIREMENT, Schema.OPTIONAL_INT32_SCHEMA);


	private Logger log = Logger.getLogger(OpenProjectSourceTask.class.getName());

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
		sourcePartition.put("url", openprojectURL);
		sourcePartition.put("project", openprojectProject);

		try {
			records.addAll(collectIssues(sourcePartition));
		} catch (Exception e) {
			log.info("Error in kafka-connect-mantis pool: " + e.getMessage() + " " + getStackTrace(e));
		}
		return records;
	}

	/**
	 * Collect Work Package Datas
	 * 
	 * @param sourcePartition
	 * @return SourceRecord
	 * @throws Exception
	 */
	private List<SourceRecord> collectIssues(HashMap<String, String> sourcePartition) throws Exception {

		List<SourceRecord> records = new ArrayList<>();

		// Find Project Id based on Projects Name
		OPElement project = null;
		OPResult projectList = OpenProjectAPI.getProjects(this.openprojectURL, OPENPROJECT_API_USER,
				this.openprojectApiKey);
		for (OPElement pr : projectList._embedded.elements) {
			if (pr.name.equals(this.openprojectProject)) {
				project = pr;
			}
		}

		// Find Project WorkPackages
		if (project != null) {
			
			// LIST updated Work Package
			OPResult lastWorkPackages = OpenProjectAPI.getWorkPackageByProject(this.openprojectURL, project.id,
					OPENPROJECT_API_USER, this.openprojectApiKey, this.lastExecution);
			for (OPElement workPackageRef : lastWorkPackages._embedded.elements) {
				OPWorkPackage workPackage = OpenProjectAPI.getWorkPackage(this.openprojectURL, workPackageRef.id,
						OPENPROJECT_API_USER, this.openprojectApiKey);

				Map<String, String> sourceOffset = new HashMap<>();
				sourceOffset.put("lastExecution", dfZULU.format(new Date()));

				Struct struct = new Struct(schema).put(FIELD_ISSUE_ID, workPackage.id)
						.put(FIELD_SUMMARY, workPackage.subject).put(FIELD_DESCRIPTION, workPackage.description.raw)
						.put(FIELD_ISSUE_TYPE, workPackage._embedded.type.name)
						.put(FIELD_OPENPROJECT_URL, this.openprojectURL + workPackage._links.self.href)
						.put(FIELD_PROJECT_ID, getProjectId(workPackage._links.project.href))
						.put(FIELD_PROJECT_NAME, workPackage._links.project.title)
						.put(FIELD_VERSION, workPackage._links.version.title)
						.put(FIELD_PARENT_ID, getWorkPackageId(workPackage._links.parent.href))
						.put(FIELD_PARENT_SUMMARY, workPackage._links.parent.title)
						.put(FIELD_CREATOR, workPackage._links.author.title)
						.put(FIELD_RESPONSIBLE, workPackage._links.responsible.title)
						.put(FIELD_ASSIGNEE, workPackage._links.assignee.title)
						.put(FIELD_CREATED, fornatedDate(workPackage.createdAt))
						.put(FIELD_UPDATED, fornatedDate(workPackage.updatedAt))
						.put(FIELD_START_DATE, fornatedDate(workPackage.startDate))
						.put(FIELD_DUE_DATE, fornatedDate(workPackage.dueDate))
						.put(FIELD_TIME_ESTIMATE, durationToHours(workPackage.estimatedTime))
						.put(FIELD_TIME_SPENT, durationToHours(workPackage.spentTime))
						.put(FIELD_REMAINING_TIME, durationToHours(workPackage.remainingTime))
						.put(FIELD_PERCENTAGE_DONE, workPackage.percentageDone)
						.put(FIELD_PRIORITY, workPackage._links.priority.title)
						.put(FIELD_STATUS, workPackage._links.status.title)
						.put(FIELD_ONTIME, isOnTime(workPackage));

				SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.openprojectTopic, schema,
						struct);
				records.add(sr);
			}
			
			
			// CREATE INSTANT STATS
			OPResult allWorkPackage = OpenProjectAPI.getWorkPackageByProject(this.openprojectURL, project.id,
					OPENPROJECT_API_USER, this.openprojectApiKey, null);
			
			int totalprogressspec = 0;
			int planedeffortsspec = 0;
			
			int totalprogresstask = 0;
			int planedeffortstask = 0;
			
			int nbOpenTask = 0;
			int nbClosedTask = 0;
			int nbOpenSpecification = 0;
			int nbClosedSpecification = 0;
			int nbOpenFeature = 0;
			int nbClosedFeature = 0;
			int nbTotalQR = 0;
			int nbOpenQR = 0;
			int nbRejectedQR = 0;
			int nbDerivedQR = 0;
			int nbClosedQR = 0;
			
			for (OPElement workPackageRef : allWorkPackage._embedded.elements) {
				OPWorkPackage workPackage = OpenProjectAPI.getWorkPackage(this.openprojectURL, workPackageRef.id,OPENPROJECT_API_USER, this.openprojectApiKey);
				
				if("Feature".equals(workPackage._embedded.type.name)) {
					if("Closed".equals(workPackage._links.status.title)) {
						nbClosedFeature++;
					}else {
						nbOpenFeature++;
					}
					
				}else if("Specification".equals(workPackage._embedded.type.name)) {
					if("Closed".equals(workPackage._links.status.title)) {
						nbClosedSpecification++;
					}else {
						nbOpenSpecification++;
					}
					
					if(workPackage.percentageDone == 100) {
						totalprogressspec = totalprogressspec + durationToHours(workPackage.spentTime);
						planedeffortsspec = planedeffortsspec  + durationToHours(workPackage.spentTime);
					}else {
						if(workPackage.remainingTime != null && workPackage.spentTime != null) {
							planedeffortsspec = planedeffortsspec + durationToHours(workPackage.spentTime) + durationToHours(workPackage.remainingTime);
							totalprogressspec = totalprogressspec	+  durationToHours(workPackage.spentTime);
						}else {
							int estiamted =  durationToHours(workPackage.estimatedTime);
							int progress = estiamted  * (workPackage.percentageDone  / 100);
							
							planedeffortsspec = planedeffortsspec + estiamted;
							totalprogressspec = totalprogressspec + progress;
						}						
					}								
				} else if("Task".equals(workPackage._embedded.type.name)) {
					if("Closed".equals(workPackage._links.status.title)) {
						nbClosedTask++;
					}else {
						nbOpenTask++;
					}
					
				
					
					if(workPackage.percentageDone == 100) {
						totalprogresstask = totalprogresstask + durationToHours(workPackage.spentTime);
						planedeffortstask = planedeffortstask + durationToHours(workPackage.spentTime);
					}else {			
						if(workPackage.remainingTime != null && workPackage.spentTime != null) {
							planedeffortstask = planedeffortstask + durationToHours(workPackage.spentTime) + durationToHours(workPackage.remainingTime);
							totalprogresstask = totalprogresstask	+  durationToHours(workPackage.spentTime);
						}else {
							int estiamted =  durationToHours(workPackage.estimatedTime);
							int progress = estiamted  * (workPackage.percentageDone  / 100);
							
							planedeffortstask = planedeffortstask + estiamted;
							totalprogresstask = totalprogresstask + progress;
						}											
					}
					
	
				} else if("QualityRequirement".equals(workPackage._embedded.type.name)) {
					nbTotalQR ++;
					if("Closed".equals(workPackage._links.status.title)) {
						nbClosedQR++;
					}else if("Rejected".equals(workPackage._links.status.title)) {
						nbRejectedQR++;
					}else {
						nbOpenQR++;
					}
					
					if(workPackage._links.children != null && workPackage._links.children.length > 0) {
						nbDerivedQR++;
					}
				} 

			}
			
			if(planedeffortsspec == 0) {
				planedeffortsspec = 1;
			}
			
			if(planedeffortstask == 0) {
				planedeffortstask = 1;
			}
					
			double spec_progress = new Double(totalprogressspec) / new Double(planedeffortsspec);
			double task_progress = new Double(totalprogresstask) / new Double(planedeffortstask);		
			double project_progress = (new Double(totalprogressspec) + new Double(totalprogresstask))  / (new Double(planedeffortsspec) + new Double(planedeffortstask));
			
			Struct struct = new Struct(schemastat)
					.put(POSTDATE, fornatedDate(new Date()))
					.put(FIELD_PROJECT_NAME,  this.openprojectProject)
					.put(PROJECT_PROGRESS,  project_progress)
					.put(SPECIFICATION_PROGRESS, spec_progress)
					.put(TASK_PROGRESS, task_progress)
					.put(OPEN_TASK, nbOpenTask)
					.put(CLOSED_TASK, nbClosedTask)
					.put(OPEN_SPECIFICATION, nbOpenSpecification)
					.put(CLOSED_SPECIFICATION, nbClosedSpecification)
					.put(OPEN_FEATURE, nbOpenFeature)
					.put(CLOSED_FEATURE, nbClosedFeature)
					.put(QUALITY_REQUIREMENT, nbTotalQR)
					.put(OPEN_QUALITY_REQUIREMENT, nbOpenQR)
					.put(REJECTED_QUALITY_REQUIREMENT, nbRejectedQR)
					.put(CLOSED_QUALITY_REQUIREMENT, nbClosedQR)
					.put(DERIVED_QUALITY_REQUIREMENT, nbDerivedQR);
			
			Map<String, String> sourceOffset = new HashMap<>();
			sourceOffset.put("lastExecution", dfZULU.format(new Date()));
			SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.statTopic, schemastat,struct);
			records.add(sr);
			
			log.info("Statistic :" + struct.toString());
		}

		log.info("Found " + records.size() + " updated work packages.");

		this.lastExecution = new Date();

		return records;
	}

	private String isOnTime(OPWorkPackage workPackage) {
		if(workPackage.dueDate != null  && workPackage.dueDate.getTime() < new Date().getTime() && workPackage.percentageDone != 100) {
			return "F";
		}
		return "T";
	}

	private int durationToHours(String time) {
		if(time != null) {
			String stime = time.replace("P", "").replace("H", "").replace("T", "").replace("S", "");
			try {
				if (stime.contains("D")) {
					String[] daysSplit = stime.split("D");
					int h = Integer.valueOf(daysSplit[0]) * 24;
					if(daysSplit.length > 1) {
						h = h + Integer.valueOf(daysSplit[1]);
					}
					return h;
				} else {
					return Integer.valueOf(stime);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}

	private String fornatedDate(Date date) {
		if (date != null) {
			return dfZULU.format(date);
		}
		return null;
	}

	private Integer getWorkPackageId(String workPackageHref) {
		if (workPackageHref != null) {
			String idStr = workPackageHref.replace("/openproject/api/v3/work_packages/", "");
			return Integer.valueOf(idStr);
		}
		return null;
	}

	private Integer getProjectId(String projectHref) {
		String idStr = projectHref.replace("/openproject/api/v3/projects/", "");
		return Integer.valueOf(idStr);
	}

	@Override
	public void start(Map<String, String> props) {
		log.info("kafka-connect-mantis: start");

		this.openprojectURL = props.get(OpenProjectSourceConnector.OPENPROJECT_URL_CONFIG);
		this.openprojectApiKey = props.get(OpenProjectSourceConnector.OPENPROJECT_APIKEY);
		this.openprojectProject = props.get(OpenProjectSourceConnector.OPENPROJECT_PROJECT_CONFIG);
		this.openprojectTopic = props.get(OpenProjectSourceConnector.OPENPROJECT_TOPIC_CONFIG);
		this.statTopic  = props.get(OpenProjectSourceConnector.OPENPROJECT_STAT_TOPIC_CONFIG);

		String intSecs = props.get(OpenProjectSourceConnector.POLL_INTERVAL_SECONDS_CONFIG);

		if (intSecs != null) {
			interval = Integer.parseInt(intSecs);
		} else {
			interval = 60;
		}

		HashMap<String, String> sourcePartition = new HashMap<>();
		sourcePartition.put("url", openprojectURL);
		sourcePartition.put("project", openprojectProject);

		if (context != null) {
			Map<String, Object> offset = context.offsetStorageReader().offset(sourcePartition);
			if (offset != null) {
				try {
					this.lastExecution = dfZULU.parse((String) offset.get("lastExecution"));
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

}
