package model.taiga;

import java.util.Date;

public class Epic {

    public String subject;
    public Integer id;
    public Boolean is_closed;
    public Integer status;
    public StatusExtraInfo status_extra_info;
    public UserStoriesCounts user_stories_counts;

    public Date created_date;
    public Date modified_date;
}
