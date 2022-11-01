

package connect.sonarCloud;

import com.google.gson.Gson;
import model.sonarqube.issues.SonarCloudIssuesResult;
import model.sonarqube.measures.SonarCloudMeasuresResult;
import rest.RESTInvoker;

/**
 * REST calls for Sonarqube data collection
 * @author wickenkamp
 *
 */
public class SonarCloudApi {
	
	
	public static SonarCloudMeasuresResult getMeasures(String sonarURL, String username, String password, String metricKeys, String sonarBaseComponentKey, int pageIndex) {
		
		RESTInvoker ri = new RESTInvoker(sonarURL + "/api/measures/component_tree?" + "metricKeys=" + metricKeys + "&baseComponentKey=" + sonarBaseComponentKey + "&pageIndex=" + pageIndex , username, password);
		
		Gson  gson = new Gson();

		return gson.fromJson(ri.getDataFromServer(""), SonarCloudMeasuresResult.class);
	}
	
	public static SonarCloudIssuesResult getIssues(String sonarUrl, String username, String password, String projectKeys, int p) {
		
		RESTInvoker ri = new RESTInvoker(sonarUrl + "/api/issues/search?projectKeys=" + projectKeys + "&p=" + p, username, password);
		
		Gson  gson = new Gson();

		return gson.fromJson(ri.getDataFromServer(""), SonarCloudIssuesResult.class);
	}
	
}
