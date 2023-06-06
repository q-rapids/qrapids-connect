package connect.taiga;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;
import model.taiga.Epic;
import model.taiga.Issue;
import model.taiga.Milestone;
import model.taiga.Project;
import model.taiga.Task;
import model.taiga.UserStory;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

public class TaigaSourceTask extends SourceTask {
    private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    private String version = "0.0.1";
    private String taigaUrl;
    private String taigaSlug;
    private String taigaUser;
    private String taigaPass;
    private String taigaToken;
    private String taigaProjectId;
    private String issuetopic;
    private String epictopic;
    private String userstorytopic;
    private String tasktopic;
    private String taigaInterval;
    private Integer interval;
    private String taigaTaskCustomAttributes;
    private String taigaUserstoryCustomAttributes;
    private Integer oldEpicSize = 0;
    private Integer count = 0;
    private String createdSince;
    private String updatedSince;
    private Date mostRecentUpdate;
    private static DateFormat onlyDate;
    private long lastPoll = 0L;
    private Boolean firstPoll = true;
    private Logger log = Logger.getLogger(TaigaSourceTask.class.getName());

    public TaigaSourceTask() {
    }

    private Boolean followsPattern(String description) {
        String a = "AS";
        String b = "I WANT";
        String c = "SO THAT";
        description = description.toUpperCase();
        return description.contains("AS") && description.contains("I WANT") && description.contains("SO THAT") || description.contains("COM A") && description.contains("VULL") && description.contains("DE MANERA QUE") || description.contains("COMO") && description.contains("QUIERO") && description.contains("DE MANERA QUE");
    }

    private Boolean hasAcceptanceCriteria(String criteria) {
        return criteria.contains("1.") || criteria.contains("*");
    }

    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> records = new ArrayList();
        this.log.info("lastPollDeltaMillis:" + (System.currentTimeMillis() - this.lastPoll) + " interval:" + this.interval);

        if (this.lastPoll != 0L && System.currentTimeMillis() < this.lastPoll + (long)(this.interval * 1000)) {
            this.log.info("------- exit polling, " + (System.currentTimeMillis() - this.lastPoll) / 1000L + " secs since last poll.");
            Thread.sleep(1000L);
            return records;
        }
        else {
            this.lastPoll = System.currentTimeMillis();
            int offset = 1;
            if (this.firstPoll) {
                if (this.mostRecentUpdate != null) {
                    this.updatedSince = onlyDate.format(this.mostRecentUpdate);
                } else {
                    this.updatedSince = "2000-01-01";

                    try {
                        this.mostRecentUpdate = onlyDate.parse(this.updatedSince);
                    } catch (ParseException e) {
                        this.log.info("unable to parse " + this.updatedSince);
                        throw new InterruptedException();
                    }
                }
            } else {
                this.log.info("Query updated since: " + this.mostRecentUpdate);
            }

            int total_issues = 0;
            Date maxUpdatedOn = null;
            if (!this.taigaUser.equals("XXXX") && !this.taigaUser.equals(" ") && !this.taigaUser.equals("")) {
                this.taigaToken = TaigaApi.Login(this.taigaUrl, this.taigaUser, this.taigaPass);
            } else {
                this.taigaToken = null;
            }

            this.taigaProjectId = String.valueOf(TaigaApi.getProjectId(this.taigaUrl, this.taigaSlug, this.taigaToken));
            Project myProject = TaigaApi.getProject(this.taigaUrl, this.taigaProjectId, this.taigaToken);
            Issue[] redmineIssues = TaigaApi.getIssuesByProjectId(this.taigaUrl, this.taigaProjectId, this.taigaToken);
            if (maxUpdatedOn == null && redmineIssues.length > 0) {
                maxUpdatedOn = redmineIssues[0].modified_date;
            }

            total_issues += redmineIssues.length;
            records.addAll(this.getIssueSourceRecords(redmineIssues, this.mostRecentUpdate, myProject));
            ++offset;
            Map<Integer, Milestone> myMilestones = null;
            Epic[] myEpics = TaigaApi.getEpicsByProjectID(this.taigaUrl, this.taigaProjectId, this.taigaToken);
            UserStory[] myUserStories = TaigaApi.getUserStoriesByProjectId(this.taigaUrl, this.taigaProjectId, this.taigaToken);
            Task[] myTasks = TaigaApi.getTasks(this.taigaUrl, this.taigaProjectId, this.taigaToken);
            myMilestones = TaigaApi.getMilestonesByProjectId(this.taigaUrl, this.taigaProjectId, this.taigaToken);
            String[] taskCustomAttributes = this.taigaTaskCustomAttributes.split(",");

            for(int i = 0; i < taskCustomAttributes.length; ++i) {
                taskCustomAttributes[i] = taskCustomAttributes[i].toUpperCase();
            }

            String[] storyCustomAttributes = this.taigaUserstoryCustomAttributes.split(",");

            for(int i = 0; i < storyCustomAttributes.length; ++i) {
                storyCustomAttributes[i] = storyCustomAttributes[i].toUpperCase();
            }

            Map<Integer, String> taskAttributesIDs = TaigaApi.getCustomAttributesIDs(this.taigaUrl, "task", this.taigaProjectId, this.taigaToken);
            Map<Integer, String> userstoryAttributesIDs = TaigaApi.getCustomAttributesIDs(this.taigaUrl, "userstory", this.taigaProjectId, this.taigaToken);
            records.addAll(this.getTaigaEpics(myEpics));
            records.addAll(this.getTaigaUserStories(myUserStories, myMilestones, userstoryAttributesIDs));
            records.addAll(this.getTaigaTasks(myTasks, taskAttributesIDs, myMilestones));
            this.mostRecentUpdate = maxUpdatedOn;
            this.firstPoll = false;
            return records;
        }
    }

    private List<SourceRecord> getTaigaTasks(Task[] tasks, Map<Integer, String> taskAttributesIDs, Map<Integer, Milestone> milestones) throws InterruptedException {
        List<SourceRecord> result = new ArrayList();

        for(Task t : tasks) {
            if (this.count == 5) {
                Thread.sleep(10L);
                this.count = 0;
            }

            this.count = this.count + 1;
            this.log.info("TASK ID: " + t.id);
            this.log.info("TASK URL: " + this.taigaUrl + "/task/" + t.id);
            Struct tasktemp = new Struct(TaigaSchema.taigaTask);
            tasktemp.put("subject", t.subject);
            if (t.user_story != null) {
                tasktemp.put("user_story_id", t.user_story);
            }

            tasktemp.put("id", t.id);
            tasktemp.put("status", t.status_extra_info.name);
            tasktemp.put("is_closed", t.is_closed);
            tasktemp.put("reference", t.ref);
            if (t.assigned_to_extra_info != null) {
                tasktemp.put("assigned", t.assigned_to_extra_info.username);
            }

            tasktemp.put("created_date", dfZULU.format(t.created_date));
            if (t.modified_date != null) {
                tasktemp.put("modified_date", dfZULU.format(t.modified_date));
            }

            if (t.finished_date != null) {
                tasktemp.put("finished_date", dfZULU.format(t.finished_date));
            }

            Map<String, String> temp = TaigaApi.getCustomAttributes(t.id, "task", this.taigaUrl, this.taigaToken, taskAttributesIDs);
            if (temp.get("ESTIMATED EFFORT") != null) {
                tasktemp.put("estimated_effort", Float.parseFloat(temp.get("ESTIMATED EFFORT")));
            } else if (temp.get("ESFORÇ ESTIMAT") != null) {
                tasktemp.put("estimated_effort", Float.parseFloat(temp.get("ESFORÇ ESTIMAT")));
            } else if (temp.get("ESFUERZO ESTIMADO") != null) {
                tasktemp.put("estimated_effort", Float.parseFloat(temp.get("ESFUERZO ESTIMADO")));
            }

            if (temp.get("ACTUAL EFFORT") != null) {
                tasktemp.put("actual_effort", Float.parseFloat(temp.get("ACTUAL EFFORT")));
            } else if (temp.get("ESFORÇ REAL") != null) {
                tasktemp.put("actual_effort", Float.parseFloat(temp.get("ESFORÇ REAL")));
            } else if (temp.get("ESFUERZO REAL") != null) {
                tasktemp.put("actual_effort", Float.parseFloat(temp.get("ESFUERZO REAL")));
            }

            if (t.milestone != null) {
                tasktemp.put("milestone_id", (milestones.get(t.milestone)).id);
                tasktemp.put("milestone_name", (milestones.get(t.milestone)).name);
                tasktemp.put("milestone_closed_points", (milestones.get(t.milestone)).closed_points);
                tasktemp.put("milestone_total_points", (milestones.get(t.milestone)).total_points);
                LocalDateTime now = LocalDateTime.now();

                try {
                    Date today = onlyDate.parse(now.toString());
                    if (!(milestones.get(t.milestone)).closed && (milestones.get(t.milestone)).estimated_start.compareTo(today) < 0) {
                        tasktemp.put("milestone_closed", (milestones.get(t.milestone)).closed);
                    } else {
                        tasktemp.put("milestone_closed", true);
                    }
                } catch (ParseException e) {
                    tasktemp.put("milestone_closed", (milestones.get(t.milestone)).closed);
                    this.log.info("unable to parse " + now.toString());
                    throw new InterruptedException();
                }

                tasktemp.put("milestone_created_date", dfZULU.format((milestones.get(t.milestone)).created_date));
                if ((milestones.get(t.milestone)).modified_date != null) {
                    tasktemp.put("milestone_modified_date", dfZULU.format((milestones.get(t.milestone)).modified_date));
                }

                if ((milestones.get(t.milestone)).estimated_start != null) {
                    tasktemp.put("estimated_start", dfZULU.format((milestones.get(t.milestone)).estimated_start));
                }

                if ((milestones.get(t.milestone)).estimated_finish != null) {
                    tasktemp.put("estimated_finish", dfZULU.format((milestones.get(t.milestone)).estimated_finish));
                }
            }

            Map<String, String> sourcePartition = new HashMap();
            sourcePartition.put("taigaUrl", this.taigaUrl);
            Map<String, String> sourceOffset = new HashMap();
            sourceOffset.put("updated", dfZULU.format(t.modified_date));
            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.tasktopic, Schema.STRING_SCHEMA, t.id.toString(), TaigaSchema.taigaTask, tasktemp);
            result.add(sr);
        }

        return result;
    }

    private List<SourceRecord> getTaigaUserStories(UserStory[] us, Map<Integer, Milestone> milestones, Map<Integer, String> userstoryAttributesIDs) throws InterruptedException {
        List<SourceRecord> result = new ArrayList();

        for(int x = 0; x < us.length; ++x) {
            if (this.count == 5) {
                Thread.sleep(10L);
                this.count = 0;
            }

            this.count = this.count + 1;
            this.log.info("US ID: " + us[x].id);
            this.log.info("US URL: " + this.taigaUrl + "/userstory/" + us[x].id);
            Struct ustemp = new Struct(TaigaSchema.taigaUserStory);
            ustemp.put("subject", us[x].subject);
            if (us[x].epics != null) {
                ustemp.put("epic_id", us[x].epics[0].id);
            }

            ustemp.put("id", us[x].id);
            ustemp.put("status", us[x].status_extra_info.name);
            ustemp.put("is_closed", us[x].is_closed);
            ustemp.put("reference", us[x].ref);
            if (us[x].assigned_to_extra_info != null) {
                ustemp.put("assigned", us[x].assigned_to_extra_info.username);
            }

            ustemp.put("created_date", dfZULU.format(us[x].created_date));
            if (us[x].modified_date != null) {
                ustemp.put("modified_date", dfZULU.format(us[x].modified_date));
            }

            if (us[x].finish_date != null) {
                ustemp.put("finished_date", dfZULU.format(us[x].finish_date));
            }

            ustemp.put("total_points", us[x].total_points);
            UserStory temp = TaigaApi.getUserStoryById(this.taigaUrl, us[x].id.toString(), this.taigaToken);
            ustemp.put("pattern", this.followsPattern(temp.description));
            Map<String, String> tempMap = TaigaApi.getCustomAttributes(us[x].id, "userstory", this.taigaUrl, this.taigaToken, userstoryAttributesIDs);
            if (tempMap.get("ACCEPTANCE CRITERIA") != null) {
                ustemp.put("acceptance_criteria", this.hasAcceptanceCriteria(tempMap.get("ACCEPTANCE CRITERIA")));
            } else if (tempMap.get("CRITERIS D'ACCEPTACIÓ") != null) {
                ustemp.put("acceptance_criteria", this.hasAcceptanceCriteria(tempMap.get("CRITERIS D'ACCEPTACIÓ")));
            } else if (tempMap.get("CRITERIOS DE ACEPTACIÓN") != null) {
                ustemp.put("acceptance_criteria", this.hasAcceptanceCriteria(tempMap.get("CRITERIOS DE ACEPTACIÓN")));
            } else {
                ustemp.put("acceptance_criteria", false);
            }

            if (tempMap.get("PRIORITY") != null) {
                ustemp.put("priority", tempMap.get("PRIORITY"));
            } else if (tempMap.get("PRIORITAT") != null) {
                ustemp.put("priority", tempMap.get("PRIORITAT"));
            } else if (tempMap.get("PRIORIDAD") != null) {
                ustemp.put("priority", tempMap.get("PRIORIDAD"));
            }

            if (us[x].milestone != null) {
                ustemp.put("milestone_id", (milestones.get(us[x].milestone)).id);
                ustemp.put("milestone_name", (milestones.get(us[x].milestone)).name);
                ustemp.put("milestone_closed_points", (milestones.get(us[x].milestone)).closed_points);
                ustemp.put("milestone_total_points", (milestones.get(us[x].milestone)).total_points);
                LocalDateTime now = LocalDateTime.now();

                try {
                    Date today = onlyDate.parse(now.toString());
                    if (!(milestones.get(us[x].milestone)).closed && (milestones.get(us[x].milestone)).estimated_start.compareTo(today) < 0) {
                        ustemp.put("milestone_closed", (milestones.get(us[x].milestone)).closed);
                    } else {
                        ustemp.put("milestone_closed", true);
                    }
                } catch (ParseException e) {
                    ustemp.put("milestone_closed", (milestones.get(us[x].milestone)).closed);
                    this.log.info("unable to parse " + now.toString());
                    throw new InterruptedException();
                }

                ustemp.put("milestone_created_date", dfZULU.format((milestones.get(us[x].milestone)).created_date));
                if ((milestones.get(us[x].milestone)).modified_date != null) {
                    ustemp.put("milestone_modified_date", dfZULU.format((milestones.get(us[x].milestone)).modified_date));
                }

                if ((milestones.get(us[x].milestone)).estimated_start != null) {
                    ustemp.put("estimated_start", dfZULU.format((milestones.get(us[x].milestone)).estimated_start));
                }

                if ((milestones.get(us[x].milestone)).estimated_finish != null) {
                    ustemp.put("estimated_finish", dfZULU.format((milestones.get(us[x].milestone)).estimated_finish));
                }
            }

            Map<String, String> sourcePartition = new HashMap();
            sourcePartition.put("taigaUrl", this.taigaUrl);
            Map<String, String> sourceOffset = new HashMap();
            sourceOffset.put("updated", dfZULU.format(us[x].modified_date));
            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.userstorytopic, Schema.STRING_SCHEMA, us[x].id.toString(), TaigaSchema.taigaUserStory, ustemp);
            result.add(sr);
        }

        return result;
    }

    private List<SourceRecord> getTaigaEpics(Epic[] epics) throws InterruptedException {
        List<SourceRecord> result = new ArrayList();
        for(Epic e : epics) {
            if (this.count == 5) {
                Thread.sleep(10L);
                this.count = 0;
            }

            this.count = this.count + 1;
            this.log.info("EPIC ID: " + e.id);
            this.log.info("EPIC URL: " + this.taigaUrl + "/epics/" + e.id);
            Struct struct = new Struct(TaigaSchema.taigaEpic);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_ID, e.id);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_SUBJECT, e.subject);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_STATUS, e.status_extra_info.name);
            struct.put(TaigaSchema.FIELD_TAIGA_EPIC_IS_CLOSED, e.is_closed);
            if (e.user_stories_counts != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_PROGRESS, e.user_stories_counts.progress);
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_TOTAL, e.user_stories_counts.total);
            }

            if (e.created_date != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_CREATED_DATE, dfZULU.format(e.created_date));
            }

            if (e.modified_date != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_EPIC_MODIFIED_DATE, dfZULU.format(e.modified_date));
            }

            Map<String, String> sourcePartition = new HashMap();
            sourcePartition.put("taigaUrl", this.taigaUrl);
            Map<String, String> sourceOffset = new HashMap();
            sourceOffset.put("updated", dfZULU.format(e.modified_date));
            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.epictopic, Schema.STRING_SCHEMA, e.id.toString(), TaigaSchema.taigaEpic, struct);
            result.add(sr);
        }

        return result;
    }

    private List<SourceRecord> getIssueSourceRecords(Issue[] redmineIssues, Date updatedSince, Project myProject) throws InterruptedException {
        List<SourceRecord> result = new ArrayList();

        for(Issue i : redmineIssues) {
            if (this.count == 5) {
                Thread.sleep(10L);
                this.count = 0;
            }

            this.count = this.count + 1;
            this.log.info("ISSUE ID: " + i.id);
            this.log.info("ISSUE URL: " + this.taigaUrl + "/issues/" + i.id);
            Struct struct = new Struct(TaigaSchema.taigaIssue);
            struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_SUBJECT, i.subject);
            struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_DESCRIPTION, i.description);
            struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_ID, i.id);
            struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_STATUS, i.status_extra_info.name);
            struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_IS_CLOSED, i.is_closed);
            if (i.created_date != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_CREATED_DATE, dfZULU.format(i.created_date));
            }

            if (i.modified_date != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_MODIFIED_DATE, dfZULU.format(i.modified_date));
            }

            if (i.finished_date != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_FINISHED_DATE, dfZULU.format(i.finished_date));
            }

            if (i.assigned_to_extra_info != null) {
                struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_ASSIGNED, i.assigned_to_extra_info.username);
            }

            int x = 0;

            boolean found;
            for(found = false; !found && x < myProject.severities.length; ++x) {
                if (myProject.severities[x].id.equals(i.severity)) {
                    found = true;
                    struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_SEVERITY, myProject.severities[x].name);
                }
            }

            x = 0;

            for(found = false; !found && x < myProject.priorities.length; ++x) {
                if (myProject.priorities[x].id.equals(i.priority)) {
                    found = true;
                    struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_PRIORITY, myProject.priorities[x].name);
                }
            }

            x = 0;

            for(found = false; !found && x < myProject.issue_types.length; ++x) {
                if (myProject.issue_types[x].id.equals(i.type)) {
                    found = true;
                    struct.put(TaigaSchema.FIELD_TAIGA_ISSUE_TYPE, myProject.issue_types[x].name);
                }
            }

            Map<String, String> sourcePartition = new HashMap();
            sourcePartition.put("taigaUrl", this.taigaUrl);
            Map<String, String> sourceOffset = new HashMap();
            sourceOffset.put("updated", dfZULU.format(i.modified_date));
            SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, this.issuetopic, Schema.STRING_SCHEMA, i.id.toString(), TaigaSchema.taigaIssue, struct);
            result.add(sr);
        }

        this.log.info("Found " + result.size() + " issues.");
        return result;
    }

    public void start(Map<String, String> props) {
        this.log.info("connect-taiga: start");
        this.log.info(props.toString());
        this.taigaUrl = props.get("taiga.url");
        this.taigaUser = props.get("username");
        this.taigaPass = props.get("password");
        this.taigaSlug = props.get("slug");
        this.issuetopic = props.get("taiga.issue.topic");
        this.epictopic = props.get("taiga.epic.topic");
        this.userstorytopic = props.get("taiga.userstory.topic");
        this.tasktopic = props.get("taiga.task.topic");
        this.taigaInterval = props.get("taiga.interval.seconds");
        this.taigaTaskCustomAttributes = props.get("taskCustomAttributes");
        this.taigaUserstoryCustomAttributes = props.get("userstoryCustomAttributes");
        this.log.info("taiga.url: " + this.taigaUrl);
        this.log.info("taiga.interval.seconds: " + this.taigaInterval);
        if (this.taigaInterval != null && !this.taigaInterval.isEmpty()) {
            this.interval = Integer.parseInt(this.taigaInterval);
        } else {
            this.interval = 3600;
        }

        Map<String, String> sourcePartition = new HashMap();
        sourcePartition.put("taigaUrl", this.taigaUrl);
        if (this.context != null) {
            Map<String, Object> offset = this.context.offsetStorageReader().offset(sourcePartition);
            if (offset != null) {
                try {
                    this.mostRecentUpdate = dfZULU.parse((String)offset.get("updated"));
                    this.log.info("--------------------------found offset: updated=" + this.mostRecentUpdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void stop() {
    }

    public String version() {
        return this.version;
    }

    static {
        dfZULU.setTimeZone(tzUTC);
        onlyDate = new SimpleDateFormat("yyyy-MM-dd");
    }
}
