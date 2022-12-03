/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonar.issues;

import model.sonar.Paging;

/**
 * SonarCloudIssuesResult API result
 * @author Axel Wickenkamp & Max Tiessler
 *
 */
public class SonarCloudIssuesResult {

	public int total;
	public int p;
	public int ps;
	public Paging paging;
	public Issue issues[];

}
