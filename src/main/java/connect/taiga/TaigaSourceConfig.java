package connect.taiga;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class TaigaSourceConfig extends AbstractConfig {

    public static final String TAIGA_URL_CONFIG = "taiga.url";
    public static final String TAIGA_URL_CONFIG_DISPLAY = "taiga server url.";
    public static final String TAIGA_URL_CONFIG_DOC = "The URL of the taiga API.";

    public static final String TAIGA_CREATED_SINCE_CONFIG = "taiga.created.since";
    public static final String TAIGA_CREATED_SINCE_CONFIG_DISPLAY = "Issues created since.";
    public static final String TAIGA_CREATED_SINCE_CONFIG_DOC = "Fetch only issues that where created since, e.g. '2017-01-31'.";

    public static final String TAIGA_USER_CONFIG = "username";
    public static final String TAIGA_USER_CONFIG_DISPLAY = "taiga username";
    public static final String TAIGA_USER_CONFIG_DOC = "Username to use when connecting taiga.";

    public static final String TAIGA_PASS_CONFIG = "password";
    public static final String TAIGA_PASS_CONFIG_DISPLAY = "Password";
    public static final String TAIGA_PASS_CONFIG_DOC = "Password to connect to taiga.";

    public static final String TAIGA_TOKEN_CONFIG = "token";
    public static final String TAIGA_TOKEN_CONFIG_DISPLAY = "taiga token";
    public static final String TAIGA_TOKEN_CONFIG_DOC = "token to use when connecting taiga.";

    public static final String TAIGA_REFRESH_CONFIG = "refresh";
    public static final String TAIGA_REFRESH_CONFIG_DISPLAY = "taiga refresh";
    public static final String TAIGA_REFRESH_CONFIG_DOC = "token to refresh the taiga token.";

    public static final String TAIGA_SLUG_CONFIG = "slug";
    public static final String TAIGA_SLUG_CONFIG_DISPLAY = "taiga project slug";
    public static final String TAIGA_SLUG_CONFIG_DOC = "Slug of the wanted project";
 
    public static final String TAIGA_ISSUE_TOPIC_CONFIG = "taiga.issue.topic";
    public static final String TAIGA_ISSUE_TOPIC_CONFIG_DISPLAY = "Topic to persist taiga Issues.";
    public static final String TAIGA_ISSUE_TOPIC_CONFIG_DOC = "Topic to persist taiga Issues.";
    public static final String TAIGA_ISSUE_TOPIC_CONFIG_DEFAULT = "taiga.issue.topic";

    public static final String TAIGA_EPIC_TOPIC_CONFIG = "taiga.epic.topic";
    public static final String TAIGA_EPIC_TOPIC_CONFIG_DISPLAY = "Topic to persist taiga epic.";
    public static final String TAIGA_EPIC_TOPIC_CONFIG_DOC = "Topic to persist taiga epic.";
    public static final String TAIGA_EPIC_TOPIC_CONFIG_DEFAULT = "taiga.metric.epic";

    public static final String TAIGA_USERSTORY_TOPIC_CONFIG = "taiga.userstory.topic";
    public static final String TAIGA_USERSTORY_TOPIC_CONFIG_DISPLAY = "Topic to persist taiga userstory.";
    public static final String TAIGA_USERSTORY_TOPIC_CONFIG_DOC = "Topic to persist taiga userstory.";
    public static final String TAIGA_USERSTORY_TOPIC_CONFIG_DEFAULT = "taiga.metric.userstory";

    public static final String TAIGA_TASK_TOPIC_CONFIG = "taiga.task.topic";
    public static final String TAIGA_TASK_TOPIC_CONFIG_DISPLAY = "Topic to persist taiga task.";
    public static final String TAIGA_TASK_TOPIC_CONFIG_DOC = "Topic to persist taiga task.";
    public static final String TAIGA_TASK_TOPIC_CONFIG_DEFAULT = "taiga.metric.task";


    public static final String TAIGA_INTERVAL_SECONDS_CONFIG = "taiga.interval.seconds";
    public static final String TAIGA_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String TAIGA_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int    TAIGA_INTERVAL_SECONDS_CONFIG_DEFAULT = 24 * 60 * 60;

    public static final String TAIGA_GROUP = "TAIGA";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(TAIGA_URL_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, TAIGA_URL_CONFIG_DOC, TAIGA_GROUP, 1, ConfigDef.Width.LONG, TAIGA_URL_CONFIG_DISPLAY)
                .define(TAIGA_CREATED_SINCE_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_CREATED_SINCE_CONFIG_DOC, TAIGA_GROUP, 4, ConfigDef.Width.MEDIUM, TAIGA_CREATED_SINCE_CONFIG_DISPLAY)
                .define(TAIGA_USER_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_USER_CONFIG_DOC, TAIGA_GROUP, 1, ConfigDef.Width.MEDIUM, TAIGA_USER_CONFIG_DISPLAY)
                .define(TAIGA_PASS_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_PASS_CONFIG_DOC, TAIGA_GROUP, 2, ConfigDef.Width.MEDIUM, TAIGA_PASS_CONFIG_DISPLAY)
                .define(TAIGA_SLUG_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_SLUG_CONFIG_DOC, TAIGA_GROUP, 3, ConfigDef.Width.MEDIUM, TAIGA_SLUG_CONFIG_DISPLAY)
                .define(TAIGA_ISSUE_TOPIC_CONFIG, ConfigDef.Type.STRING, TAIGA_ISSUE_TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TAIGA_ISSUE_TOPIC_CONFIG_DOC, TAIGA_GROUP, 5, ConfigDef.Width.MEDIUM, TAIGA_ISSUE_TOPIC_CONFIG_DISPLAY)
                .define(TAIGA_INTERVAL_SECONDS_CONFIG, ConfigDef.Type.LONG, TAIGA_INTERVAL_SECONDS_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TAIGA_INTERVAL_SECONDS_CONFIG_DOC, TAIGA_GROUP, 6, ConfigDef.Width.SHORT,  TAIGA_INTERVAL_SECONDS_CONFIG_DISPLAY)
                .define(TAIGA_EPIC_TOPIC_CONFIG, ConfigDef.Type.STRING, TAIGA_EPIC_TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TAIGA_EPIC_TOPIC_CONFIG_DOC, TAIGA_GROUP, 9, ConfigDef.Width.MEDIUM, TAIGA_EPIC_TOPIC_CONFIG_DISPLAY)
                .define(TAIGA_USERSTORY_TOPIC_CONFIG, ConfigDef.Type.STRING, TAIGA_USERSTORY_TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TAIGA_USERSTORY_TOPIC_CONFIG_DOC, TAIGA_GROUP, 10, ConfigDef.Width.MEDIUM, TAIGA_USERSTORY_TOPIC_CONFIG_DISPLAY)
                .define(TAIGA_TASK_TOPIC_CONFIG, ConfigDef.Type.STRING, TAIGA_TASK_TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TAIGA_TASK_TOPIC_CONFIG_DOC, TAIGA_GROUP, 11, ConfigDef.Width.MEDIUM, TAIGA_TASK_TOPIC_CONFIG_DISPLAY)
                .define(TAIGA_TOKEN_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_TOKEN_CONFIG_DOC, TAIGA_GROUP, 8, ConfigDef.Width.MEDIUM, TAIGA_TOKEN_CONFIG_DISPLAY)
                .define(TAIGA_REFRESH_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_REFRESH_CONFIG_DOC, TAIGA_GROUP, 8, ConfigDef.Width.MEDIUM, TAIGA_REFRESH_CONFIG_DISPLAY);
    }

    public TaigaSourceConfig(Map<String, String> originals) { super(DEFS, originals); }
}

//.define(TAIGA_SECRET_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, TAIGA_SECRET_CONFIG_DOC, TAIGA_GROUP, 3, ConfigDef.Width.MEDIUM, TAIGA_SECRET_CONFIG_DISPLAY)

