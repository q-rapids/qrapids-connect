package model.taiga;

import java.util.Date;

public class Issue {
    public String subject;
    public String description;
    public Integer id;
    public Integer severity;
    public Integer status;
    public Integer priority;
    public Integer type;
    public Boolean is_closed;
    
    public StatusExtraInfo status_extra_info;
    public AssignedToExtraInfo  assigned_to_extra_info;

    public Date created_date;
    public Date modified_date;
    public Date finished_date;

    public Date due_date;
    public String due_date_reason;
}
