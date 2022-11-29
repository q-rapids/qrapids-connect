/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonarCloud.measures;

/**
 * Sonarqube Measure API result
 * @author Axel Wickenkamp & Max Tiessler
 *
 */
public class Measure {
	
	public String metric;
	public String value;
	
	public Period periods[];
}
