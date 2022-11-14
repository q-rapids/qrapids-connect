package connect.sonar;

import org.apache.kafka.connect.source.SourceRecord;
import util.PropertyFile;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Run the SonarSourceTask without Kafka
 * @author Max Tiessler
 *
 */
public class Driver {
	private static final Logger log = LoggerFactory.getLogger(Driver.class.getName());

	public static void main(String[] args) throws InterruptedException {
		
		Map<String,String> config = PropertyFile.get("config/connect-sonar.properties");
		SonarSourceTask st = new SonarSourceTask();
		st.start(config);
		while(true) {
			List<SourceRecord> l = st.poll();
			for (SourceRecord sr : l) {
				log.info("Source record: {}", sr);
			}
		}
		
	}

}

