package connect.github;

import com.google.gson.Gson;
import model.github.*;
import model.github.commit.Commit;
import model.github.commit.CommitStats;
import rest.RESTInvoker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class GithubApi {

	public enum State {
		ALL("all"), OPEN("open"), CLOSED("closed");
		State(String v) {
			value = v;
		}
		private final String value;
		public String getValue() {
			return value;
		}
	}

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

	static {
		dfZULU.setTimeZone(tzUTC);
	}

	private static Gson gson = new Gson();

	public static GithubIssues getIssues(String url, String secret, String updatedSince, State state, int offset) {

		String api = "/issues";
		String apiparams = "?since="+ updatedSince +"&state=" + state.getValue() + "&order_by=updated_at&sort=desc&per_page=100&page=" + offset;
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		model.github.Issue[] iss = gson.fromJson(json, model.github.Issue[].class);

		GithubIssues giss = new GithubIssues();
		giss.issues=iss;
		giss.total_count = (long) iss.length;
		giss.offset = (long) offset;
		return giss;
	}

	// Returns all users that contributed to the repo and their n# of contributions
	public static GithubContributor getContributors(String url, String secret, boolean anon, int offset) {

		String api = "/contributors";
		String apiparams = "?anon=" + anon + "&page=" + offset;
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		model.github.User[] con = gson.fromJson(json, model.github.User[].class);

		GithubContributor gcon = new GithubContributor();
		gcon.users=con;
		gcon.total_count = (long) con.length;
		gcon.offset = (long) offset;
		return gcon;
	}

	// Returns all repository labels
	public static GithubLabels getLabels(String url, String secret, int offset) {

		String api = "/labels";
		String apiparams = "?page=" + offset;
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		model.github.Label[] la = gson.fromJson(json, model.github.Label[].class);

		GithubLabels gla = new GithubLabels();
		gla.labels=la;
		gla.total_count = (long) la.length;
		gla.offset = (long) offset;
		return gla;
	}

	// Returns the repository specified by the url
	public static Repository getRepository(String url, String secret) {

		RESTInvoker ri = new RESTInvoker(url, secret);
		String json = ri.getDataFromServer("");
        return gson.fromJson(json, Repository.class);
	}

	// Returns the repository milestones
	public static GithubMilestones getMilestones(String url, String secret, State state, int offset) {

		String api = "/milestones";
		String apiparams = "?state=all&sort=desc&page=" + offset + "&state=" + state.getValue();
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		model.github.Milestone[] mile = gson.fromJson(json, model.github.Milestone[].class);

		GithubMilestones gmile = new GithubMilestones();
		gmile.milestones=mile;
		gmile.total_count = (long) mile.length;
		gmile.offset = (long) offset;
		return gmile;
	}

	// Returns all commits made by a user
	public static GitHubCommits getCommits(String url, String secret, String user, int offset) {

		String api = "/commits";
		String apiparams = "?author=" + user + "&per_page=100&page=" + offset;
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		Commit[] commits = gson.fromJson(json, Commit[].class);

		for (Commit c: commits) {
			apiparams = "/" + c.sha;
			urlCall = url + api + apiparams;
			ri = new RESTInvoker(urlCall, secret);
			json = ri.getDataFromServer("");
			CommitStats stats = gson.fromJson(json, CommitStats.class);
			c.stats = stats.stats;
		}

		GitHubCommits gcommit = new GitHubCommits();
		gcommit.commits = commits;
		gcommit.total_count = (long) commits.length;
		gcommit.offset = (long) offset;
		return gcommit;
	}

	public static void main(String[] args) {
		GitHubCommits ri = getCommits("https://api.github.com/repos/q-rapids/qrapids-dashboard","HsdhNpJXdhpgpd7bkJtB", "alejandravv" ,1);
		for (Commit a: ri.commits) {
			System.out.println(a.committer.login + " " + a.commit.author.date + " " + a.commit.message);
		}

	}
	
}
