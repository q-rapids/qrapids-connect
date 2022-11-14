/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonarqube.issues;

import model.sonarqube.Paging;

/**
 * SonarCloud Issues API result
 * @author Max Tiessler & Axel Wickenkamp
 *
 */
public class SonarIssuesResult {
	
	public int total;
	public int p;
	public int ps;
	public Paging paging;
	public Issue issues[];

}
