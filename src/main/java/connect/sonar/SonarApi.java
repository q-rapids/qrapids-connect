

package connect.sonar;

import com.google.gson.Gson;
import model.sonarqube.issues.SonarIssuesResult;
import model.sonarqube.measures.SonarMeasuresResult;
import rest.RESTInvoker;

/**
 * REST calls for SonarCloud data collection
 * @author Max Tiessler
 *
 */
public class SonarApi {

	private SonarApi() {
		throw new IllegalStateException("Api Utility Class");
	}

	public static SonarMeasuresResult getMeasures(String cloudToken, String cloudProjectKey, String metricKeys, int pageIndex) {
		
		RESTInvoker ri = new RESTInvoker("https://"
				+ cloudToken + "@sonarcloud.io"
				+ "/api/measures/component_tree?"
				+ "component=" + cloudProjectKey
				+ "&metricKeys=" + metricKeys
				+ "&p="
				+ pageIndex,
				null);
		
		Gson  gson = new Gson();
		return gson.fromJson(ri.getDataFromServer(""), SonarMeasuresResult.class);
	}
	
	public static SonarIssuesResult getIssues(String cloudToken, String cloudProjectKeys, int p) {
		RESTInvoker ri = new RESTInvoker("https://"
				+ cloudToken + "@sonarcloud.io"
				+ "/api/issues/search?projectKeys=" + cloudProjectKeys
				+ "&p=" + p
				+ "&types=CODE_SMELL,BUG,VULNERABILITY", null);
		
		Gson  gson = new Gson();
		return gson.fromJson(ri.getDataFromServer(""), SonarIssuesResult.class);
	}
	
}
