package connect.gitlab;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


public class GitlabSourceConfig extends AbstractConfig {

    public static final String URL_CONFIG = "gitlab.url";
    public static final String URL_CONFIG_DISPLAY = "GitLab server url.";
    public static final String URL_CONFIG_DOC = "The URL of the GitLab installation.";

    public static final String SECRET_CONFIG = "gitlab.secret";
    public static final String SECRET_CONFIG_DISPLAY = "Secret token";
    public static final String SECRET_CONFIG_DOC = "Token to use when connecting to GitLab API.";
    
    public static final String CREATED_SINCE_CONFIG = "gitlab.created.since";
    public static final String CREATED_SINCE_CONFIG_DISPLAY = "Issues created since.";
    public static final String CREATED_SINCE_CONFIG_DOC = "Fetch only issues that where created since, e.g. '2017-01-31'.";
    
    public static final String TOPIC_CONFIG = "gitlab.topic";
    public static final String TOPIC_CONFIG_DISPLAY = "Topic to persist GitLab issues.";
    public static final String TOPIC_CONFIG_DOC = "Name of the Kafka Topic to which the source records (GitLab Issues) are written to.";
    public static final String TOPIC_CONFIG_DEFAULT = "gitlab";
    
    public static final String INTERVAL_SECONDS_CONFIG = "gitlab.interval.seconds";
    public static final String INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int INTERVAL_SECONDS_CONFIG_DEFAULT = 24 * 60 * 60;
    
    public static final String GROUP = "GITLAB";
    public static final String CONNECTOR_GROUP = "Connector";

    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
        .define(URL_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, URL_CONFIG_DOC, GROUP, 1, ConfigDef.Width.LONG, URL_CONFIG_DISPLAY)
        .define(SECRET_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, SECRET_CONFIG_DOC, GROUP, 3, ConfigDef.Width.MEDIUM, SECRET_CONFIG_DISPLAY)
        .define(CREATED_SINCE_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, CREATED_SINCE_CONFIG_DOC, GROUP, 4, ConfigDef.Width.MEDIUM, CREATED_SINCE_CONFIG_DISPLAY)
        .define(TOPIC_CONFIG, ConfigDef.Type.STRING, TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TOPIC_CONFIG_DOC, CONNECTOR_GROUP, 5, ConfigDef.Width.LONG, TOPIC_CONFIG_DISPLAY)
        .define(INTERVAL_SECONDS_CONFIG, ConfigDef.Type.LONG, INTERVAL_SECONDS_CONFIG_DEFAULT, ConfigDef.Importance.LOW,INTERVAL_SECONDS_CONFIG_DOC, GROUP, 6, ConfigDef.Width.SHORT, INTERVAL_SECONDS_CONFIG_DISPLAY);
    }

    public GitlabSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

    public URL getGitlabUrl() {
        try {
            return new URL(getString(URL_CONFIG));
        } catch (MalformedURLException e) {
            throw new ConfigException("Couldn't create the URL from " + getString(URL_CONFIG), e);
        }
    }

    public String getSecret() {
        return getString(SECRET_CONFIG);
    }

    public Long getIntervalSeconds() {
        return getLong(INTERVAL_SECONDS_CONFIG);
    }


}
