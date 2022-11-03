/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonarqube.issues;

/**
 * SonarCloud Issues API result
 * @author Max Tiessler & Axel Wickenkamp
 *
 */
public class Issue {
	
	public String key;
	public String rule;
	public String severity;
	public String component;
	public int componentId;
	public String project;
	public int line;
	public TextRange textRange;
	public String status;
	public String message;
	public String effort;
	public String debt;
	public String author;
	public String creationDate;
	public String updateDate;
	public String type;

}
