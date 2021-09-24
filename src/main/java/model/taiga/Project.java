package model.taiga;
import java.util.Date;

public class Project {

    public String name;
    public String description;

    public Integer id;
    public Float total_story_points;
    public Integer total_activity;
    public Integer total_activity_last_week;
    public Integer total_activity_last_year;
    public Integer total_activity_last_month;
    public Integer total_closed_milestones;
    public Float closed_points;
    public Float defined_points;
    public Float total_points;

    public Type issue_types[];
    public Priority priorities[];
    public Severity severities[];

    public Epic epics[];
    public Milestone milestones[];
    public Task task_custom_attributes[];
    public Issue issues[];
    public UserStory userStories[];

    public Date created_date;
    public Date modified_date;

}
