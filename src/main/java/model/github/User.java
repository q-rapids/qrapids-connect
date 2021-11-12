package model.github;

public class User {
	public String login;
	public long id;
	public String node_id;
	public String avatar_url;
	public String gravatar_id;
	public String url;
	public String html_url;
	public String followers_url;
	public String following_url;
	public String gists_url;
	public String starred_url;
	public String subscriptions_url;
	public String organizations_url;
	public String repos_url;
	public String events_url;
	public String received_events_url;
	public String type;
	public Boolean site_admin;

	public int contributions;

	public User(String login, int id, String url, String type, boolean site_admin) {
		this.login = login;
		this.id = id;
		this.url = url;
		this.type = type;
		this.site_admin = site_admin;
	}
}
