package connect.openproject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;

import model.openproject.OPResult;
import model.openproject.OPWorkPackage;
import rest.RESTInvoker;

public class OpenProjectAPI {


	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

	static {
		dfZULU.setTimeZone(tzUTC);
	}
	
	public static OPResult getProjects(String openPeojectUrl, String username, String password) {		
		RESTInvoker ri = new RESTInvoker(openPeojectUrl + "/api/v3/projects" , username, password);	
		Gson  gson = new Gson();	
		String json = ri.getDataFromServer("");
		return gson.fromJson(json, OPResult.class);
	}
	
	
	public static OPResult getWorkPackageByProject(String openPeojectUrl,int projectId, String username, String password, Date lastExecution) {		
		
		RESTInvoker ri = null;
		if(lastExecution != null) {
			String fromDate= dfZULU.format(lastExecution);
			String toDate= dfZULU.format(new Date());
			ri = new RESTInvoker(openPeojectUrl + "/api/v3/projects/"+projectId+"/work_packages/?filters=[{\"updatedAt\":{\"operator\":\"<>d\",\"values\":[\""+fromDate+"\",\""+toDate+"\"]}}]&pageSize=500", username, password);	

		}else {
			ri = new RESTInvoker(openPeojectUrl + "/api/v3/projects/"+projectId+"/work_packages/?filters=[]&pageSize=500", username, password);	
		}
		
		Gson  gson = new Gson();	
		String json = ri.getDataFromServer("");
		return gson.fromJson(json, OPResult.class);
	}
	
	
	
	public static OPWorkPackage getWorkPackage(String openPeojectUrl,int workPackageId, String username, String password) {		
		RESTInvoker ri = new RESTInvoker(openPeojectUrl + "/api/v3/work_packages/"+workPackageId , username, password);	
		Gson  gson = new Gson();	
		String json = ri.getDataFromServer("");
		System.out.println(json);
		return gson.fromJson(json, OPWorkPackage.class);
	}
}

