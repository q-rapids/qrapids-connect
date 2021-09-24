package model.taiga;
import java.util.Date;

public class Project {

    public String name;
    public String description;

    public Integer id;
    public Integer total_story_points;
    public Integer total_activity;
    public Integer total_activity_last_week;
    public Integer total_activity_last_year;
    public Integer total_activity_last_month;
    public Integer assigned_points;
    public Integer closed_points;
    public Integer defined_points;
    public Integer total_points;

    public Epic epics[];
    public Milestone milestones[];
    public Task task_custom_attributes[];
    public Issue issues[];
    public UserStory userStories[];

    public Date created_date;
    public Date modified_date;

}
