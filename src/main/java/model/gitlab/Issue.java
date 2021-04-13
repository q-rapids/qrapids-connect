package model.gitlab;

import java.util.Date;
import java.util.Map;

public class Issue {
	
	public Long id;
	public Long iid;	
	public Long project_id;
	public String title;
	public String description;
	public Date created_at;
	public Date updated_at;
	public Date due_date;
	public Date closed_at;
	public String state;
	public TimeStats time_stats;
	public boolean has_tasks;
	public TaskCompletion task_completion_status;
	public String[] labels;
}
