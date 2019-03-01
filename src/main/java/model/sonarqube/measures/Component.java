/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package model.sonarqube.measures;

/**
 * Component API result
 * @author Axel Wickenkamp
 *
 */
public class Component {
	
	public String id;
	public String key;
	public String name;
	public String qualifier;
	public String path;
	public String language;
	
	public Measure measures[];
	
}
