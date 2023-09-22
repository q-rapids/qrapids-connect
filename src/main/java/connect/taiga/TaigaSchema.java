package connect.taiga;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.Date;

public class TaigaSchema {

    //METRICS ESTO VA A DOLER

    //EPIC
    public static String FIELD_TAIGA_EPIC_SUBJECT = "subject";
    public static String FIELD_TAIGA_EPIC_ID = "id";
    public static String FIELD_TAIGA_EPIC_IS_CLOSED = "is_closed";
    public static String FIELD_TAIGA_EPIC_STATUS = "status";
    public static String FIELD_TAIGA_EPIC_PROGRESS = "progress";
    public static String FIELD_TAIGA_EPIC_TOTAL = "total";
    public static String FIELD_TAIGA_EPIC_CREATED_DATE = "created_date";
    public static String FIELD_TAIGA_EPIC_MODIFIED_DATE = "modified_date";
    public static String FIELD_TAIGA_EPIC_USER_STORIES = "user_stories";

    //USER STORY
    public static String FIELD_TAIGA_US_SUBJECT = "subject";
    public static String FIELD_TAIGA_US_ID = "id";
    public static String FIELD_TAIGA_US_IS_CLOSED = "is_closed";
    public static String FIELD_TAIGA_US_STATUS = "status";
    public static String FIELD_TAIGA_US_TOTAL_POINTS = "total_points";
    public static String FIELD_TAIGA_US_ASSIGNED = "assigned";
    public static String FIELD_TAIGA_US_CREATED_DATE = "created_date";
    public static String FIELD_TAIGA_US_MODIFIED_DATE = "modified_date";
    public static String FIELD_TAIGA_US_FINISHED_DATE = "finished_date";
    public static String FIELD_TAIGA_US_TASKS = "user_story_tasks";
    public static String FIELD_TAIGA_US_MILESTONES = "milestones";
    public static String FIELD_TAIGA_US_EPIC_ID = "epic_id";

    //TASK
    public static String FIELD_TAIGA_TASK_SUBJECT = "subject";
    public static String FIELD_TAIGA_TASK_ID = "id";
    public static String FIELD_TAIGA_TASK_IS_CLOSED = "is_closed";
    public static String FIELD_TAIGA_TASK_STATUS = "status";
    public static String FIELD_TAIGA_TASK_ASSIGNED= "assigned";
    public static String FIELD_TAIGA_TASK_CREATED_DATE = "created_date";
    public static String FIELD_TAIGA_TASK_MODIFIED_DATE = "modified_date";
    public static String FIELD_TAIGA_TASK_FINISHED_DATE = "finished_date";
    public static String FIELD_TAIGA_TASK_USERSTORY_ID= "user_story_id";

    //MIELSTONE
    public static String FIELD_TAIGA_MILESTONE_ID = "id";
    public static String FIELD_TAIGA_MILESTONE_NAME = "name";
    public static String FIELD_TAIGA_MILESTONE_IS_CLOSED = "is_closed";
    public static String FIELD_TAIGA_MILESTONE_CLOSED_POINTS = "closed_points";
    public static String FIELD_TAIGA_MILESTONE_TOTAL_POINTS = "total_points";
    public static String FIELD_TAIGA_MILESTONE_CREATED_DATE = "created_Date";
    public static String FIELD_TAIGA_MILESTONE_MODIFIED_DATE = "modified_date";
    public static String FIELD_TAIGA_MILESTONE_ESTIMATED_START = "estimated_start";
    public static String FIELD_TAIGA_MILESTONE_ESTIMATED_FINISH = "estimated_finish";

    //ISSUES
    public static String FIELD_TAIGA_ISSUE_SUBJECT = "subject";
    public static String FIELD_TAIGA_ISSUE_DESCRIPTION = "description";
    public static String FIELD_TAIGA_ISSUE_ID = "id";
    public static String FIELD_TAIGA_ISSUE_SEVERITY= "severity";
    public static String FIELD_TAIGA_ISSUE_STATUS = "status";
    public static String FIELD_TAIGA_ISSUE_PRIORITY = "priority";
    public static String FIELD_TAIGA_ISSUE_TYPE = "type";
    public static String FIELD_TAIGA_ISSUE_IS_CLOSED = "is_closed";
    public static String FIELD_TAIGA_ISSUE_MODIFIED_DATE = "modified_date";
    public static String FIELD_TAIGA_ISSUE_CREATED_DATE = "created_date";
    public static String FIELD_TAIGA_ISSUE_FINISHED_DATE = "finished_date";
    public static String FIELD_TAIGA_ISSUE_ASSIGNED = "assigned";

    public static Schema taigaIssue = SchemaBuilder.struct().name("taigaissue")
        .field(FIELD_TAIGA_ISSUE_SUBJECT, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_DESCRIPTION, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_ID, Schema.OPTIONAL_INT32_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_SEVERITY, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_STATUS, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_PRIORITY, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_TYPE, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_IS_CLOSED, Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_MODIFIED_DATE, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_CREATED_DATE, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_FINISHED_DATE, Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_ISSUE_ASSIGNED, Schema.OPTIONAL_STRING_SCHEMA)
        .build();

    public static Schema taigaTask = SchemaBuilder.struct().name("taigatask")
        .field("subject" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("id" ,Schema.OPTIONAL_INT32_SCHEMA)
        .field("user_story_id" ,Schema.OPTIONAL_INT32_SCHEMA)
        .field("is_closed" ,Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field("status" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("assigned" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("created_date" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("modified_date" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("finished_date" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("estimated_effort", Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field("actual_effort", Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field("reference", Schema.OPTIONAL_INT32_SCHEMA)
        .field("milestone_id" ,Schema.OPTIONAL_INT32_SCHEMA)
        .field("milestone_name" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("milestone_closed" ,Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field("milestone_closed_points" ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field("milestone_total_points" ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field("milestone_created_date" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("milestone_modified_date" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("estimated_start" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("estimated_finish" ,Schema.OPTIONAL_STRING_SCHEMA)
        .build();

    public static Schema taigaMilestone = SchemaBuilder.struct().name("taigamilestone")
        .field(FIELD_TAIGA_MILESTONE_ID ,Schema.OPTIONAL_INT32_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_NAME ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_IS_CLOSED ,Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_CLOSED_POINTS ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_TOTAL_POINTS ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_CREATED_DATE ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_MODIFIED_DATE ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_ESTIMATED_START ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_MILESTONE_ESTIMATED_FINISH ,Schema.OPTIONAL_STRING_SCHEMA)
        .build();

    public static Schema taigaUserStory = SchemaBuilder.struct().name("taigauserstory")
        .field("subject" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("id" ,Schema.OPTIONAL_INT32_SCHEMA)
        .field("epic_id" ,Schema.OPTIONAL_INT32_SCHEMA)
        .field("is_closed" ,Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field("status" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("assigned" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("created_date" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("modified_date" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("finished_date" ,Schema.OPTIONAL_STRING_SCHEMA )
        .field("total_points" ,Schema.OPTIONAL_FLOAT32_SCHEMA )
        .field("reference", Schema.OPTIONAL_INT32_SCHEMA)
        .field("milestone_id" ,Schema.OPTIONAL_INT32_SCHEMA)
        .field("milestone_name" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("milestone_closed" ,Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field("milestone_closed_points" ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field("milestone_total_points" ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field("milestone_created_date" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("milestone_modified_date" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("estimated_start" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("estimated_finish" ,Schema.OPTIONAL_STRING_SCHEMA)
        .field("acceptance_criteria",Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field("pattern", Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field("priority", Schema.OPTIONAL_STRING_SCHEMA)
        .build();

    public static Schema taigaEpic = SchemaBuilder.struct().name("taigaepic")
        .field(FIELD_TAIGA_EPIC_SUBJECT ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_EPIC_ID ,Schema.OPTIONAL_INT32_SCHEMA)
        .field(FIELD_TAIGA_EPIC_IS_CLOSED ,Schema.OPTIONAL_BOOLEAN_SCHEMA)
        .field(FIELD_TAIGA_EPIC_STATUS ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_EPIC_PROGRESS ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field(FIELD_TAIGA_EPIC_TOTAL ,Schema.OPTIONAL_FLOAT32_SCHEMA)
        .field(FIELD_TAIGA_EPIC_CREATED_DATE ,Schema.OPTIONAL_STRING_SCHEMA)
        .field(FIELD_TAIGA_EPIC_MODIFIED_DATE ,Schema.OPTIONAL_STRING_SCHEMA )
        .build();
}
