package connect.github;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import model.github.Issue;


public class GithubSourceTask extends SourceTask {

	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	static {
		dfZULU.setTimeZone(tzUTC);
	}

	private String version = "0.0.1";
	
	private String githubUser;
	private String githubPass;
	private String githubIssuesTopic;
	private String githubInterval;
	private Integer interval;

	// millis of last poll
	private long lastPoll = 0;

	private Logger log = Logger.getLogger(GithubSourceTask.class.getName());

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		//TODO: IMPLEMENT!
		return null;
	}

	private List<SourceRecord>  getSourceRecords( Issue[] issues ) {
		//TODO: IMPLEMENT!
		return null;
	}

	
	
	
	
	@Override
	public void start(Map<String, String> props) {

		log.info("connect-github: start");
		log.info(props.toString());

		githubUser 				= props.get( GithubSourceConfig.GITHUB_USER_CONFIG );
		githubPass 				= props.get( GithubSourceConfig.GITHUB_PASS_CONFIG );
		githubIssuesTopic 		= props.get( GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG );
		githubInterval = props.get( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG );
		
		if ( (githubInterval == null || githubInterval.isEmpty()) ) {
			interval = 3600;
		} else {
			interval = Integer.parseInt(githubInterval);
		}

	}

	@Override
	public void stop() {

	}

	public String version() {
		return version;
	}

}
