/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonarqube.measures;

import model.sonarqube.Paging;

/**
 * SonarcubeMeasuresResult API result
 * @author Axel Wickenkamp
 *
 */
public class SonarcubeMeasuresResult {
	
	public Paging paging;
	
	public Component baseComponent;
	public Component components[];
	

}
