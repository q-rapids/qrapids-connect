package connect.jira;


import com.google.gson.Gson;

import model.jira.JQLResult;
import rest.RESTInvoker;

public class JiraApi {
	
	
	public static JQLResult getIssues(String jiraUrl, String username, String password, String jql, int startAt) {
		
		RESTInvoker ri = new RESTInvoker(jiraUrl + "/rest/api/2/search?jql=" + jql + "&startAt=" + startAt , username, password);
		
		Gson  gson = new Gson();
		
		String json = ri.getDataFromServer("");
		
		/**
		try {
			PrintWriter debug = new PrintWriter("jira.debug.txt");
			debug.print(json);
			debug.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}**/

		return gson.fromJson(json, JQLResult.class);
	}

}
