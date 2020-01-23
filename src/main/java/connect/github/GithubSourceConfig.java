/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.github;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 * configuration properties of the GithubSourceConnector
 *
 * @author Axel Wickenkamp
 */
public class GithubSourceConfig extends AbstractConfig {

    public static final String GITHUB_USER_CONFIG = "username";
    public static final String GITHUB_USER_CONFIG_DISPLAY = "Github username";
    public static final String GITHUB_USER_CONFIG_DOC = "Username to use when connecting Github.";

    public static final String GITHUB_PASS_CONFIG = "password";
    public static final String GITHUB_PASS_CONFIG_DISPLAY = "Password";
    public static final String GITHUB_PASS_CONFIG_DOC = "Password to connect to Github.";

    public static final String GITHUB_ISSUES_TOPIC_CONFIG = "github.issues.topic";
    public static final String GITHUB_ISSUES_TOPIC_CONFIG_DISPLAY = "Topic to persist github Issues.";
    public static final String GITHUB_ISSUES_TOPIC_CONFIG_DOC = "Topic to persist github Issues.";
    public static final String GITHUB_ISSUES_TOPIC_CONFIG_DEFAULT = "github.issues.topic";
    
    public static final String GITHUB_INTERVAL_SECONDS_CONFIG = "github.interval.seconds";
    public static final String GITHUB_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String GITHUB_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int    GITHUB_INTERVAL_SECONDS_CONFIG_DEFAULT = 3600;

    public static final String GITHUB_GROUP = "GITHUB";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(GITHUB_USER_CONFIG, 			ConfigDef.Type.STRING, "", 										ConfigDef.Importance.LOW, GITHUB_USER_CONFIG_DOC, 				GITHUB_GROUP, 1, ConfigDef.Width.MEDIUM, GITHUB_USER_CONFIG_DISPLAY)
                .define(GITHUB_PASS_CONFIG, 			ConfigDef.Type.STRING, "", 										ConfigDef.Importance.LOW, GITHUB_PASS_CONFIG, 					GITHUB_GROUP, 2, ConfigDef.Width.MEDIUM, GITHUB_PASS_CONFIG_DISPLAY)
                .define(GITHUB_ISSUES_TOPIC_CONFIG, 	ConfigDef.Type.STRING, GITHUB_ISSUES_TOPIC_CONFIG_DEFAULT, 		ConfigDef.Importance.LOW, GITHUB_ISSUES_TOPIC_CONFIG_DOC, 		GITHUB_GROUP, 3, ConfigDef.Width.MEDIUM, GITHUB_ISSUES_TOPIC_CONFIG_DISPLAY)
                .define(GITHUB_INTERVAL_SECONDS_CONFIG, ConfigDef.Type.LONG, GITHUB_INTERVAL_SECONDS_CONFIG_DEFAULT, 	ConfigDef.Importance.LOW, GITHUB_INTERVAL_SECONDS_CONFIG_DOC, 	GITHUB_GROUP, 4, ConfigDef.Width.SHORT,  GITHUB_INTERVAL_SECONDS_CONFIG_DISPLAY);
    }

    public GithubSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

}
