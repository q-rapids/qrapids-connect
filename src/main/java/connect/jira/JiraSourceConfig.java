package connect.jira;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * configuration properties of the JiraSourceConnector
 *
 * @author Axel Wickenkamp
 */
public class JiraSourceConfig extends AbstractConfig {

    public static final String JIRA_URL_CONFIG = "jira.url";
    public static final String JIRA_URL_CONFIG_DISPLAY = "JIRA server url.";
    public static final String JIRA_URL_CONFIG_DOC = "The URL of the JIRA installation.";
    
    public static final String JIRA_PROJECT_CONFIG = "jira.project";
    public static final String JIRA_PROJECT_CONFIG_DISPLAY = "Jira Project";
    public static final String JIRA_PROJECT_CONFIG_DOC = "Jira Project.";

    public static final String JIRA_USER_CONFIG = "jira.user";
    public static final String JIRA_USER_CONFIG_DISPLAY = "Jira Username.";
    public static final String JIRA_USER_CONFIG_DOC = "Username to use when connecting Jira.";

    public static final String JIRA_PASS_CONFIG = "jira.pass";
    public static final String JIRA_PASS_CONFIG_DISPLAY = "Password";
    public static final String JIRA_PASS_CONFIG_DOC = "Password to use when connecting to Jira.";
    
    public static final String JIRA_CREATED_SINCE_CONFIG = "jira.created.since";
    public static final String JIRA_CREATED_SINCE_CONFIG_DISPLAY = "Issues created since.";
    public static final String JIRA_CREATED_SINCE_CONFIG_DOC = "Fetch only issues that where created since, e.g. '2017-01-31'.";

    public static final String JIRA_QUERY_CONFIG = "jira.query";
    public static final String JIRA_QUERY_CONFIG_DISPLAY = "Jira API query.";
    public static final String JIRA_QUERY_CONFIG_DOC = "Jira API query.";

    
    public static final String JIRA_TOPIC_CONFIG = "jira.topic";
    public static final String JIRA_TOPIC_CONFIG_DISPLAY = "Topic to persist JIRA issues.";
    public static final String JIRA_TOPIC_CONFIG_DOC = "Name of the Kafka Topic to which the source records (JIRA Issues) are written to.";
    public static final String JIRA_TOPIC_CONFIG_DEFAULT = "jira.connector.topic";
    
    public static final String JIRA_INTERVAL_SECONDS_CONFIG = "jira.interval.seconds";
    public static final String JIRA_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String JIRA_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int JIRA_INTERVAL_SECONDS_CONFIG_DEFAULT = 600;
    

    public static final String JIRA_GROUP = "JIRA";
    public static final String CONNECTOR_GROUP = "Connector";

    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(JIRA_URL_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, JIRA_URL_CONFIG_DOC, JIRA_GROUP, 1, ConfigDef.Width.LONG, JIRA_URL_CONFIG_DISPLAY)
                .define(JIRA_USER_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JIRA_USER_CONFIG_DOC, JIRA_GROUP, 2, ConfigDef.Width.MEDIUM, JIRA_USER_CONFIG_DISPLAY)
                .define(JIRA_PASS_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JIRA_PASS_CONFIG_DOC, JIRA_GROUP, 3, ConfigDef.Width.MEDIUM, JIRA_PASS_CONFIG_DISPLAY)
                .define(JIRA_QUERY_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JIRA_QUERY_CONFIG_DOC, JIRA_GROUP, 3, ConfigDef.Width.MEDIUM, JIRA_QUERY_CONFIG_DISPLAY)
                .define(JIRA_CREATED_SINCE_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JIRA_CREATED_SINCE_CONFIG_DOC, JIRA_GROUP, 3, ConfigDef.Width.MEDIUM, JIRA_PASS_CONFIG_DISPLAY)
                .define(JIRA_TOPIC_CONFIG, ConfigDef.Type.STRING, JIRA_TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, JIRA_TOPIC_CONFIG_DOC, CONNECTOR_GROUP, 4, ConfigDef.Width.LONG, JIRA_TOPIC_CONFIG_DISPLAY)
                .define(JIRA_INTERVAL_SECONDS_CONFIG, ConfigDef.Type.LONG, JIRA_INTERVAL_SECONDS_CONFIG_DEFAULT, ConfigDef.Importance.LOW, JIRA_INTERVAL_SECONDS_CONFIG_DOC, JIRA_GROUP, 6, ConfigDef.Width.SHORT, JIRA_INTERVAL_SECONDS_CONFIG_DISPLAY);
    }

    public JiraSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

    public URL getJiraUrl() {
        try {
            return new URL(getString(JIRA_URL_CONFIG));
        } catch (MalformedURLException e) {
            throw new ConfigException("Couldn't create the URL from " + getString(JIRA_URL_CONFIG), e);
        }
    }

    public String getUsername() {
        return getString(JIRA_USER_CONFIG);
    }

    public String getPasswordOrApiToken() {
        return getString(JIRA_PASS_CONFIG);
    }

    public boolean isProtected() {
        return getString(JIRA_PASS_CONFIG) != null && !getString(JIRA_PASS_CONFIG).isEmpty();
    }
    
    public Long getIntervalSeconds() {
    	return getLong(JIRA_INTERVAL_SECONDS_CONFIG);
    }


}
