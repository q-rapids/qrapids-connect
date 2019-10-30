package connect.jira;

import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.source.SourceRecord;

import util.PropertyFile;

public class Driver {
	
	public static void main(String[] args) throws InterruptedException {
		
		Map<String, String> config = PropertyFile.get("./config/connect-jira-source.properties");
		
		JiraSourceTask st = new JiraSourceTask();
		st.start(config);
		while(true) {
			List<SourceRecord> l = st.poll();
			
			for (SourceRecord sr : l) {
				System.out.println(sr);
			}
			
		}

		
	}

}
