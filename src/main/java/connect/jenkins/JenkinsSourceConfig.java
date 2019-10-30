package connect.jenkins;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * configuration properties of the JenkinsSourceConnector
 *
 * @author Axel Wickenkamp
 */
public class JenkinsSourceConfig extends AbstractConfig {

    public static final String JENKINS_URL_CONFIG = "jenkins.url";
    public static final String JENKINS_URL_CONFIG_DISPLAY = "Jenkins server url.";
    public static final String JENKINS_URL_CONFIG_DOC = "This is the URL of the home page of your Jenkins installation.";

    public static final String JENKINS_USER_CONFIG = "jenkins.user";
    public static final String JENKINS_USER_CONFIG_DISPLAY = "Jenkins Username.";
    public static final String JENKINS_USER_CONFIG_DOC = "Username to use when connecting to protected Jenkins.";

    public static final String JENKINS_PASS_CONFIG = "jenkins.pass";
    public static final String JENKINS_PASS_CONFIG_DISPLAY = "Password (or API Token)";
    public static final String JENKINS_PASS_CONFIG_DOC = "Password (or API Token) to use when connecting to protected Jenkins.";

    public static final String JENKINS_TOPIC_CONFIG = "jenkins.topic";
    public static final String JENKINS_TOPIC_CONFIG_DISPLAY = "Topic to persist build events.";
    public static final String JENKINS_TOPIC_CONFIG_DOC = "This is the name of the Kafka Topic to which the source records containing Jenkins Build details are written to.";
    public static final String JENKINS_TOPIC_CONFIG_DEFAULT = "jenkins.connector.topic";
    
    public static final String JENKINS_AUTH_CONFIG = "use.preemptive.auth";
    public static final String JENKINS_AUTH_CONFIG_DISPLAY = "Use special authentication method (Nokia->true).";
    public static final String JENKINS_AUTH_CONFIG_DOC = "Nokia special authentication method..";
    public static final String JENKINS_AUTH_CONFIG_DEFAULT = "false";
    
    public static final String JENKINS_JOBS_CONFIG = "jenkins.jobs";
    public static final String JENKINS_JOBS_CONFIG_DISPLAY = "List of jobs to observe.";
    public static final String JENKINS_JOBS_CONFIG_DOC = "List of jobs to observe.";
    
    
    public static final String JENKINS_INTERVAL_SECONDS_CONFIG = "jenkins.interval.seconds";
    public static final String JENKINS_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String JENKINS_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int JENKINS_INTERVAL_SECONDS_CONFIG_DEFAULT = 60;
    
    public static final String JENKINS_LOOKBACK_CONFIG = "jenkins.lookback";
    public static final String JENKINS_LOOKBACK_CONFIG_DISPLAY = "How many builds from the past are retrieved at first start.";
    public static final String JENKINS_LOOKBACK_CONFIG_DOC = "How many builds from the past are retrieved at first start.";
    public static final int JENKINS_LOOKBACK_CONFIG_DEFAULT = 10;

    public static final String JENKINS_GROUP = "Jenkins";
    public static final String CONNECTOR_GROUP = "Connector";

    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(JENKINS_URL_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, JENKINS_URL_CONFIG_DOC, JENKINS_GROUP, 1, ConfigDef.Width.LONG, JENKINS_URL_CONFIG_DISPLAY)
                .define(JENKINS_USER_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_USER_CONFIG_DOC, JENKINS_GROUP, 2, ConfigDef.Width.MEDIUM, JENKINS_USER_CONFIG_DISPLAY)
                .define(JENKINS_PASS_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_PASS_CONFIG_DOC, JENKINS_GROUP, 3, ConfigDef.Width.MEDIUM, JENKINS_PASS_CONFIG_DISPLAY)
                .define(JENKINS_TOPIC_CONFIG, ConfigDef.Type.STRING, JENKINS_TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, JENKINS_TOPIC_CONFIG_DOC, CONNECTOR_GROUP, 4, ConfigDef.Width.LONG, JENKINS_TOPIC_CONFIG_DISPLAY)
                .define(JENKINS_AUTH_CONFIG, ConfigDef.Type.STRING, JENKINS_AUTH_CONFIG_DEFAULT, ConfigDef.Importance.LOW, JENKINS_AUTH_CONFIG_DOC, CONNECTOR_GROUP, 5, ConfigDef.Width.LONG, JENKINS_AUTH_CONFIG_DISPLAY)
                .define(JENKINS_JOBS_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_JOBS_CONFIG_DOC, CONNECTOR_GROUP,6, ConfigDef.Width.LONG, JENKINS_JOBS_CONFIG_DISPLAY)
                .define(JENKINS_INTERVAL_SECONDS_CONFIG, ConfigDef.Type.LONG, JENKINS_INTERVAL_SECONDS_CONFIG_DEFAULT, ConfigDef.Importance.LOW, JENKINS_INTERVAL_SECONDS_CONFIG_DOC, JENKINS_GROUP, 7, ConfigDef.Width.SHORT, JENKINS_INTERVAL_SECONDS_CONFIG_DISPLAY)
                .define(JENKINS_LOOKBACK_CONFIG, ConfigDef.Type.LONG, JENKINS_LOOKBACK_CONFIG_DEFAULT, ConfigDef.Importance.LOW, JENKINS_LOOKBACK_CONFIG_DOC, JENKINS_GROUP, 8, ConfigDef.Width.SHORT, JENKINS_INTERVAL_SECONDS_CONFIG_DISPLAY);
    }

    public JenkinsSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

    public URL getJenkinsUrl() {
        try {
            return new URL(getString(JENKINS_URL_CONFIG));
        } catch (MalformedURLException e) {
            throw new ConfigException("Couldn't create the URL from " + getString(JENKINS_URL_CONFIG), e);
        }
    }

    public String getUsername() {
        return getString(JENKINS_USER_CONFIG);
    }

    public String getPasswordOrApiToken() {
        return getString(JENKINS_PASS_CONFIG);
    }

    public boolean isProtected() {
        return getString(JENKINS_PASS_CONFIG) != null && !getString(JENKINS_PASS_CONFIG).isEmpty();
    }
    
    public long getIntervalSeconds() {
    	return getLong(JENKINS_INTERVAL_SECONDS_CONFIG);
    }


}
