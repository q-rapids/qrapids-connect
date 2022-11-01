/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonarCloud;

import org.apache.kafka.connect.source.SourceRecord;
import util.PropertyFile;

import java.util.List;
import java.util.Map;


/**
 * Run the SonarCloudSourceTask without Kafka
 * @author Max Tiessler
 *
 */
public class Driver {
	
	public static void main(String[] args) throws InterruptedException {
		
		Map<String,String> config = PropertyFile.get("./config/connect-sonarCloud.properties");
		SonarCloudSourceTask st = new SonarCloudSourceTask();
		st.start(config);
		
		while(true) {
			List<SourceRecord> l = st.poll();
			
			for (SourceRecord sr : l) {
				System.out.println(sr);
			}
		}
		
	}

}

