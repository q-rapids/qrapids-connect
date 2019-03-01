/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonarqube;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 * configuration properties of the JiraSourceConnector
 *
 * @author Axel Wickenkamp
 */
public class SonarqubeSourceConfig extends AbstractConfig {

    public static final String SONAR_URL_CONFIG = "sonar.url";
    public static final String SONAR_URL_CONFIG_DISPLAY = "Sonar server url.";
    public static final String SONAR_URL_CONFIG_DOC = "The URL of the Sonar installation.";

    public static final String SONAR_USER_CONFIG = "sonar.user";
    public static final String SONAR_USER_CONFIG_DISPLAY = "Jira Username.";
    public static final String SONAR_USER_CONFIG_DOC = "Username to use when connecting Jira.";

    public static final String SONAR_PASS_CONFIG = "sonar.pass";
    public static final String SONAR_PASS_CONFIG_DISPLAY = "Password";
    public static final String SONAR_PASS_CONFIG_DOC = "Password to use when connecting to Jira.";
    
    // for measures
    public static final String SONAR_BCK_CONFIG = "sonar.basecomponent.key";
    public static final String SONAR_BCK_CONFIG_DISPLAY = "Sonar Base Component Key.";
    public static final String SONAR_BCK_CONFIG_DOC = "Sonar Base Component Key.";
    
    
    // for issues
    public static final String SONAR_PROJECT_KEYS_CONFIG = "sonar.project.key";
    public static final String SONAR_PROJECT_KEYS_CONFIG_DISPLAY = "Sonar project keys for issue collection.";
    public static final String SONAR_PROJECT_KEYS_CONFIG_DOC = "Sonar project keys for issue collection.";
    
    public static final String SONAR_METRIKKEYS_CONFIG = "sonar.metric.keys";
    public static final String SONAR_METRIKKEYS_CONFIG_DISPLAY = "sonar.metric.keys";
    public static final String SONAR_METRIKKEYS_CONFIG_DOC = "Sonar Metric Keys.";
    public static final String SONAR_METRIKKEYS_CONFIG_DEFAULT = "ncloc,lines,comment_lines,complexity,violations,open_issues,confirmed_issues,reopened_issues,code_smells,new_code_smells,sqale_index,new_technical_debt,bugs,new_bugs,reliability_rating,classes,functions";
    

    public static final String SONAR_MEASURE_TOPIC_CONFIG = "sonar.measure.topic";
    public static final String SONAR_MEASURE_TOPIC_CONFIG_DISPLAY = "Topic to persist Sonar Measures.";
    public static final String SONAR_MEASURE_TOPIC_CONFIG_DOC = "Topic to persist Sonar Measures.";
    public static final String SONAR_MEASURE_TOPIC_CONFIG_DEFAULT = "sonar.measure.topic";
    
    public static final String SONAR_ISSUE_TOPIC_CONFIG = "sonar.issue.topic";
    public static final String SONAR_ISSUE_TOPIC_CONFIG_DISPLAY = "Topic to persist Sonar Issues.";
    public static final String SONAR_ISSUE_TOPIC_CONFIG_DOC = "Topic to persist Sonar Issues.";
    public static final String SONAR_ISSUE_TOPIC_CONFIG_DEFAULT = "sonar.issue.topic";
    
    public static final String SONAR_INTERVAL_SECONDS_CONFIG = "sonar.interval.seconds";
    public static final String SONAR_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String SONAR_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int    SONAR_INTERVAL_SECONDS_CONFIG_DEFAULT = 3600;
    
    public static final String SONAR_SNAPSHOTDATE_CONFIG = "sonar.snapshotDate";
    public static final String SONAR_SNAPSHOTDATE_CONFIG_DISPLAY = "User defined snapshotDate.";
    public static final String SONAR_SNAPSHOTDATE_CONFIG_DOC = "User defined snapshotDate.";
    

    public static final String SONAR_GROUP = "SONAR";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(SONAR_URL_CONFIG, 				ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, 	SONAR_URL_CONFIG_DOC, 		SONAR_GROUP, 1, ConfigDef.Width.LONG, SONAR_URL_CONFIG_DISPLAY)
                .define(SONAR_USER_CONFIG, 				ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, 	SONAR_USER_CONFIG_DOC, 		SONAR_GROUP, 2, ConfigDef.Width.MEDIUM, SONAR_USER_CONFIG_DISPLAY)
                .define(SONAR_PASS_CONFIG, 				ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, 	SONAR_PASS_CONFIG, 			SONAR_GROUP, 3, ConfigDef.Width.MEDIUM, SONAR_PASS_CONFIG_DISPLAY)
                .define(SONAR_BCK_CONFIG, 				ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, 	SONAR_BCK_CONFIG_DOC, 		SONAR_GROUP, 4, ConfigDef.Width.MEDIUM, SONAR_BCK_CONFIG_DISPLAY)
                .define(SONAR_PROJECT_KEYS_CONFIG, 		ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, 	SONAR_PROJECT_KEYS_CONFIG_DOC, SONAR_GROUP, 5, ConfigDef.Width.MEDIUM, SONAR_PROJECT_KEYS_CONFIG_DISPLAY)
                .define(SONAR_METRIKKEYS_CONFIG, 		ConfigDef.Type.STRING, 	SONAR_METRIKKEYS_CONFIG_DEFAULT, 		ConfigDef.Importance.LOW, SONAR_METRIKKEYS_CONFIG_DOC, 		SONAR_GROUP, 6, ConfigDef.Width.LONG, SONAR_METRIKKEYS_CONFIG_DISPLAY)
                .define(SONAR_MEASURE_TOPIC_CONFIG, 	ConfigDef.Type.STRING, 	SONAR_MEASURE_TOPIC_CONFIG_DEFAULT, 	ConfigDef.Importance.LOW, SONAR_MEASURE_TOPIC_CONFIG_DOC, 	SONAR_GROUP, 7, ConfigDef.Width.MEDIUM, SONAR_MEASURE_TOPIC_CONFIG_DISPLAY)
                .define(SONAR_ISSUE_TOPIC_CONFIG, 		ConfigDef.Type.STRING, 	SONAR_ISSUE_TOPIC_CONFIG_DEFAULT, 		ConfigDef.Importance.LOW, SONAR_ISSUE_TOPIC_CONFIG_DOC, 	SONAR_GROUP, 8, ConfigDef.Width.MEDIUM, SONAR_ISSUE_TOPIC_CONFIG_DISPLAY)
                .define(SONAR_INTERVAL_SECONDS_CONFIG, 	ConfigDef.Type.LONG, 	SONAR_INTERVAL_SECONDS_CONFIG_DEFAULT, 	ConfigDef.Importance.LOW, SONAR_INTERVAL_SECONDS_CONFIG_DOC, SONAR_GROUP, 9, ConfigDef.Width.SHORT, SONAR_INTERVAL_SECONDS_CONFIG_DISPLAY)
                .define(SONAR_SNAPSHOTDATE_CONFIG, 	    ConfigDef.Type.STRING, "2010-20-20", ConfigDef.Importance.HIGH,    SONAR_SNAPSHOTDATE_CONFIG_DOC, SONAR_GROUP,10, ConfigDef.Width.MEDIUM, SONAR_SNAPSHOTDATE_CONFIG_DISPLAY);
    }

    public SonarqubeSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }


}
