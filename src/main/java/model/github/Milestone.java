package model.github;

public class Milestone {
	String url;
	String html_url;
	String labels_url;
	Long id;
	String node_id;
	Long number;
	String title;
	String description;
	
	User creator;
	
	Long open_issues;
	Long closed_issues;
	
	// timestamps, e.g. "2019-10-11T08:24:05Z"
	String state;
	String created_at;
	String updated_at;
	String due_on;
	String closed_at;
}
