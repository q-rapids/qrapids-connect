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

    public static final String GITHUB_ASW_URL_CONFIG = "github.asw.url";
    public static final String GITHUB_ASW_URL_CONFIG_DISPLAY = "GitHub ASW server url.";
    public static final String GITHUB_ASW_URL_CONFIG_DOC = "The URL of the GitHub ASW installation.";

    public static final String GITHUB_PES_URL_CONFIG = "github.pes.url";
    public static final String GITHUB_PES_URL_CONFIG_DISPLAY = "GitHub PES server url.";
    public static final String GITHUB_PES_URL_CONFIG_DOC = "The URL of the GitHub PES installation.";

    public static final String GITHUB_ASW_SECRET_CONFIG = "github.asw.secret";
    public static final String GITHUB_ASW_SECRET_CONFIG_DISPLAY = "Secret ASW token";
    public static final String GITHUB_ASW_SECRET_CONFIG_DOC = "Token to use when connecting to ASW GitHub API.";

    public static final String GITHUB_PES_SECRET_CONFIG = "github.pes.secret";
    public static final String GITHUB_PES_SECRET_CONFIG_DISPLAY = "Secret PES token";
    public static final String GITHUB_PES_SECRET_CONFIG_DOC = "Token to use when connecting to PES GitHub API.";

    public static final String GITHUB_CREATED_SINCE_CONFIG = "github.created.since";
    public static final String GITHUB_CREATED_SINCE_CONFIG_DISPLAY = "Issues created since.";
    public static final String GITHUB_CREATED_SINCE_CONFIG_DOC = "Fetch only issues that where created since, e.g. '2017-01-31'.";

    public static final String GITHUB_USER_CONFIG = "username";
    public static final String GITHUB_USER_CONFIG_DISPLAY = "Github username";
    public static final String GITHUB_USER_CONFIG_DOC = "Username to use when connecting Github.";

    public static final String GITHUB_PASS_CONFIG = "password";
    public static final String GITHUB_PASS_CONFIG_DISPLAY = "Password";
    public static final String GITHUB_PASS_CONFIG_DOC = "Password to connect to Github.";

    public static final String GITHUB_ISSUES_TOPIC_CONFIG = "github.issue.topic";
    public static final String GITHUB_ISSUES_TOPIC_CONFIG_DISPLAY = "Topic to persist github Issues.";
    public static final String GITHUB_ISSUES_TOPIC_CONFIG_DOC = "Topic to persist github Issues.";
    public static final String GITHUB_ISSUES_TOPIC_CONFIG_DEFAULT = "github.issue.topic";

    public static final String GITHUB_COMMIT_TOPIC_CONFIG = "github.commit.topic";
    public static final String GITHUB_COMMIT_TOPIC_CONFIG_DISPLAY = "Topic to persist github Commits.";
    public static final String GITHUB_COMMIT_TOPIC_CONFIG_DOC = "Topic to persist github Commits.";
    public static final String GITHUB_COMMIT_TOPIC_CONFIG_DEFAULT = "github.commit.topic";
    
    public static final String GITHUB_INTERVAL_SECONDS_CONFIG = "github.interval.seconds";
    public static final String GITHUB_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String GITHUB_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int    GITHUB_INTERVAL_SECONDS_CONFIG_DEFAULT = 24 * 60 * 60;

    public static final String GITHUB_GROUP = "GITHUB";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(GITHUB_ASW_URL_CONFIG,          ConfigDef.Type.STRING,  ConfigDef.Importance.HIGH, GITHUB_ASW_URL_CONFIG_DOC, GITHUB_GROUP,1, ConfigDef.Width.LONG, GITHUB_ASW_URL_CONFIG_DISPLAY)
                .define(GITHUB_PES_URL_CONFIG,          ConfigDef.Type.STRING,  ConfigDef.Importance.HIGH, GITHUB_PES_URL_CONFIG_DOC, GITHUB_GROUP,2, ConfigDef.Width.LONG, GITHUB_PES_URL_CONFIG_DISPLAY)
                .define(GITHUB_ASW_SECRET_CONFIG,       ConfigDef.Type.STRING, "",                          ConfigDef.Importance.LOW, GITHUB_ASW_SECRET_CONFIG_DOC,        GITHUB_GROUP, 5, ConfigDef.Width.MEDIUM, GITHUB_ASW_SECRET_CONFIG_DISPLAY)
                .define(GITHUB_PES_SECRET_CONFIG,       ConfigDef.Type.STRING, "",                          ConfigDef.Importance.LOW, GITHUB_PES_SECRET_CONFIG_DOC,       GITHUB_GROUP, 6, ConfigDef.Width.MEDIUM, GITHUB_PES_SECRET_CONFIG_DISPLAY)
                .define(GITHUB_CREATED_SINCE_CONFIG,    ConfigDef.Type.STRING, "",                          ConfigDef.Importance.LOW, GITHUB_CREATED_SINCE_CONFIG_DOC,    GITHUB_GROUP, 7, ConfigDef.Width.MEDIUM, GITHUB_CREATED_SINCE_CONFIG_DISPLAY)
                .define(GITHUB_USER_CONFIG,             ConfigDef.Type.STRING, "",                          ConfigDef.Importance.LOW, GITHUB_USER_CONFIG_DOC,             GITHUB_GROUP, 3, ConfigDef.Width.MEDIUM, GITHUB_USER_CONFIG_DISPLAY)
                .define(GITHUB_PASS_CONFIG,             ConfigDef.Type.STRING, "",                          ConfigDef.Importance.LOW, GITHUB_PASS_CONFIG_DOC,             GITHUB_GROUP, 4, ConfigDef.Width.MEDIUM, GITHUB_PASS_CONFIG_DISPLAY)
                .define(GITHUB_ISSUES_TOPIC_CONFIG,     ConfigDef.Type.STRING,  GITHUB_ISSUES_TOPIC_CONFIG_DEFAULT,     ConfigDef.Importance.LOW, GITHUB_ISSUES_TOPIC_CONFIG_DOC,      GITHUB_GROUP, 8, ConfigDef.Width.LONG, GITHUB_ISSUES_TOPIC_CONFIG_DISPLAY)
                .define(GITHUB_COMMIT_TOPIC_CONFIG,     ConfigDef.Type.STRING,  GITHUB_COMMIT_TOPIC_CONFIG_DEFAULT,     ConfigDef.Importance.LOW, GITHUB_COMMIT_TOPIC_CONFIG_DOC,      GITHUB_GROUP, 9, ConfigDef.Width.LONG, GITHUB_COMMIT_TOPIC_CONFIG_DISPLAY)
                .define(GITHUB_INTERVAL_SECONDS_CONFIG, ConfigDef.Type.LONG,    GITHUB_INTERVAL_SECONDS_CONFIG_DEFAULT, ConfigDef.Importance.LOW, GITHUB_INTERVAL_SECONDS_CONFIG_DOC,  GITHUB_GROUP, 10, ConfigDef.Width.SHORT,  GITHUB_INTERVAL_SECONDS_CONFIG_DISPLAY);
    }

    public GithubSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

}
