package model.taiga;
import java.util.Date;

public class Task {

    public String subject;
    public Integer id;
    public Boolean is_closed;
    public Integer milestone;
    public String milestone_slug;
    public Integer user_story;
    public Integer ref;

    public StatusExtraInfo status_extra_info;
    public AssignedToExtraInfo  assigned_to_extra_info;


    public Date due_date;
    public String due_date_reason;
    public Date created_date;
    public Date modified_date;
    public Date finished_date;
}
