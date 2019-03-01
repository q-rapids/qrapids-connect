/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonarqube;

import com.google.gson.Gson;

import model.sonarqube.issues.SonarcubeIssuesResult;
import model.sonarqube.measures.SonarcubeMeasuresResult;
import rest.RESTInvoker;

/**
 * REST calls for Sonarqube data collection
 * @author wickenkamp
 *
 */
public class SonarqubeApi {
	
	
	public static SonarcubeMeasuresResult getMeasures(String sonarURL, String username, String password, String metricKeys, String sonarBaseComponentKey, int pageIndex) {
		
		RESTInvoker ri = new RESTInvoker(sonarURL + "/api/measures/component_tree?" + "metricKeys=" + metricKeys + "&baseComponentKey=" + sonarBaseComponentKey + "&pageIndex=" + pageIndex , username, password);
		
		Gson  gson = new Gson();

		return gson.fromJson(ri.getDataFromServer(""), SonarcubeMeasuresResult.class);
	}
	
	public static SonarcubeIssuesResult getIssues(String sonarUrl, String username, String password, String projectKeys, int p) {
		
		RESTInvoker ri = new RESTInvoker(sonarUrl + "/api/issues/search?projectKeys=" + projectKeys + "&p=" + p, username, password);
		
		Gson  gson = new Gson();

		return gson.fromJson(ri.getDataFromServer(""), SonarcubeIssuesResult.class);
	}
	
}
