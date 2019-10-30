package connect.gitlab;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.google.gson.Gson;

import model.gitlab.*;
import rest.RESTInvoker;

public class GitlabApi {
	
	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	
	static {
		dfZULU.setTimeZone(tzUTC);
	}
	
	private static Gson  gson = new Gson();
	
	public static GitlabIssues getIssues(String url, String secret, String createdSince, String updatedSince, int offset) {
		
		String api = "/issues";
		String apiparams = "?order_by=updated_at&sort=desc&page=" + offset;
		String urlCall = url + api + apiparams;
		RESTInvoker ri = new RESTInvoker(urlCall, secret);
		String json = ri.getDataFromServer("");
		Issue[] iss = gson.fromJson(json, Issue[].class);
		
		GitlabIssues giss = new GitlabIssues();
		giss.issues=iss;
		giss.total_count = new Long(iss.length);
		giss.offset = new Long(offset);
		return giss;
	}
	
	public static void main(String[] args) {
		GitlabIssues ri = getIssues("http://localhost:8383/api/v4/projects/80","HsdhNpJXdhpgpd7bkJtB","2000-01-01","2000-01-01",1);
		for(Issue i : ri.issues){
		    System.out.println(i.id+" "+i.title);
		}

	}

}
