/* Copyright (C) 2019 Fraunhofer IESE
 * 
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Read property file from resource folder
 * @author Axel Wickenkamp
 *
 */
public class PropertyFile {
	
	public static Map<String,String> get(String filename) {
		
		Map<String,String> result = new HashMap<>();
		
		Properties props = new Properties();
		try {
			
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream is = classloader.getResourceAsStream(filename);
			
			props.load( is );
			
			for ( Object o : props.keySet() ) {
				result.put((String) o, props.getProperty( (String) o ) );
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

	
	public static void main(String[] args) {
		Map<String,String> props = get("./config/connect-svn-source.properties");
		System.out.println(props);
	}
}
