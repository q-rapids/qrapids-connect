package model.taiga;

import java.util.Date;

public class UserStory {

    public String subject;
    public String description;
    public Integer id;
    public Boolean is_closed;
    public Integer status;
    public Float total_points;
    public Integer milestone;
    public Epic[] epics;
    public String milestone_name;
    public String milestone_slug;
    public Integer ref;

    public StatusExtraInfo status_extra_info;
    public AssignedToExtraInfo  assigned_to_extra_info;

    public Date created_date;
    public Date modified_date;
    public Date finish_date;

    public Date due_date;
    public String due_date_reason;



}