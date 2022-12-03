/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonar.measures;

import model.sonar.Paging;

/**
 * SonarcubeMeasuresResult API result
 * @author Axel Wickenkamp & Max Tiessler
 *
 */
public class SonarCloudMeasuresResult {
	
	public Paging paging;
	
	public Component baseComponent;
	public Component components[];
	

}
