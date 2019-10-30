package model.gitlab;

import java.util.Date;

public class Issue {
	
	public Long id;
	public Long iid;	
	public Long project_id;
	public String title;
	public String description;
	public Date created_at;
	public Date updated_at;
	public String state;
	public String[] labels;
}
