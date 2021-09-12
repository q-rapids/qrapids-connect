package connect.github;

import com.google.gson.Gson;
import model.github.GithubIssues;
import rest.RESTInvoker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class GithubApi {

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

	static {
		dfZULU.setTimeZone(tzUTC);
	}

	private static Gson gson = new Gson();

	public static GithubIssues getIssues(String url, String secret, String createdSince, String updatedSince, int offset) {


		String api = "/issues";
		String apiparams = "?since="+ createdSince +"&state=all&order_by=updated_at&sort=desc&page=" + offset; // by default state is open, but we want all issues
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		model.github.Issue[] iss = gson.fromJson(json, model.github.Issue[].class);

		GithubIssues giss = new GithubIssues();
		giss.issues=iss;
		giss.total_count = new Long(iss.length);
		giss.offset = new Long(offset);
		return giss;
	}

	public static void main(String[] args) {
		GithubIssues ri = getIssues("https://api.github.com/repos/q-rapids/qrapids-dashboard","HsdhNpJXdhpgpd7bkJtB","2021-03-01","2021-03-01",1);
		for(model.github.Issue i : ri.issues){
			System.out.println(i.id+" "+i.title);
		}
	}
	
}
