/* Copyright (C) 2019 Fraunhofer IESE
 * You may use, distribute and modify this code under the
 * terms of the Apache License 2.0 license
 */

package connect.sonarCloud;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 * Configuration properties of the SonarCloudSourceConnector
 *
 * @author Max Tiessler
 */
public class SonarCloudSourceConfig extends AbstractConfig {



    /* SonarCloud API */
    public static final String CLOUD_TOKEN_CONFIG = "cloud.token";
    public static final String CLOUD_TOKEN_CONFIG_DISPLAY = "SonarCloud api token";
    public static final String CLOUD_TOKEN_CONFIG_DOC = "SonarCloud api token for API requests";

    public static final String CLOUD_ORGANIZATION_NAME_CONFIG = "cloud.organization.name";
    public static final String CLOUD_ORGANIZATION_NAME_CONFIG_DISPLAY = "SonarCloud organization name";
    public static final String CLOUD_ORGANIZATION_NAME_CONFIG_DOC = "SonarCloud organization name for API requests";

    public static final String CLOUD_ORGANIZATION_KEY_CONFIG = "cloud.organization.key";
    public static final String CLOUD_ORGANIZATION_KEY_CONFIG_DISPLAY = "SonarCloud organization key";
    public static final String CLOUD_ORGANIZATION_KEY_CONFIG_DOC = "SonarCloud organization key for API requests";

    public static final String CLOUD_PROJECT_KEYS_CONFIG = "cloud.project.key";
    public static final String CLOUD_PROJECT_KEYS_CONFIG_DISPLAY = "SonarCloud project keys for issue collection.";
    public static final String CLOUD_PROJECT_KEYS_CONFIG_DOC = "SonarCloud project keys for issue collection.";
    
    public static final String CLOUD_METRIC_KEYS_CONFIG = "cloud.metric.keys";
    public static final String CLOUD_METRIC_KEYS_CONFIG_DISPLAY = "cloud.metric.keys";
    public static final String CLOUD_METRIC_KEYS_CONFIG_DOC = "SonarCloud Metric Keys.";
    public static final String CLOUD_METRIC_KEYS_CONFIG_DEFAULT = "ncloc,lines,comment_lines,complexity,violations,open_issues,confirmed_issues,reopened_issues,code_smells,new_code_smells,sqale_index,new_technical_debt,bugs,new_bugs,reliability_rating,classes,functions";
    

    /* Kafka Topics */
    public static final String CLOUD_MEASURE_TOPIC_CONFIG = "cloud.measure.topic";
    public static final String CLOUD_MEASURE_TOPIC_CONFIG_DISPLAY = "Topic to persist SonarCloud Measures.";
    public static final String CLOUD_MEASURE_TOPIC_CONFIG_DOC = "Topic to persist SonarCloud Measures.";
    public static final String CLOUD_MEASURE_TOPIC_CONFIG_DEFAULT = "cloud.measure.topic";
    
    public static final String CLOUD_ISSUE_TOPIC_CONFIG = "cloud.issue.topic";
    public static final String CLOUD_ISSUE_TOPIC_CONFIG_DISPLAY = "Topic to persist SonarCloud Issues.";
    public static final String CLOUD_ISSUE_TOPIC_CONFIG_DOC = "Topic to persist SonarCloud Issues.";
    public static final String CLOUD_ISSUE_TOPIC_CONFIG_DEFAULT = "cloud.issue.topic";


    /* Polling */
    public static final String CLOUD_INTERVAL_SECONDS_CONFIG = "cloud.interval.seconds";
    public static final String CLOUD_INTERVAL_SECONDS_CONFIG_DISPLAY = "Polling interval in seconds.";
    public static final String CLOUD_INTERVAL_SECONDS_CONFIG_DOC = "Polling interval in seconds.";
    public static final int    SONAR_INTERVAL_SECONDS_CONFIG_DEFAULT = 3600;
    
    public static final String SONAR_SNAPSHOTDATE_CONFIG = "sonar.snapshotDate";
    public static final String SONAR_SNAPSHOTDATE_CONFIG_DISPLAY = "User defined snapshotDate.";
    public static final String SONAR_SNAPSHOTDATE_CONFIG_DOC = "User defined snapshotDate.";
    

    public static final String SONAR_GROUP = "SONAR";


    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(CLOUD_TOKEN_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.HIGH,
                        CLOUD_TOKEN_CONFIG_DOC,
                        SONAR_GROUP,
                        1,
                        ConfigDef.Width.LONG,
                        CLOUD_TOKEN_CONFIG_DISPLAY)
                .define(CLOUD_ORGANIZATION_NAME_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        CLOUD_ORGANIZATION_NAME_CONFIG_DOC,
                        SONAR_GROUP,
                        2,
                        ConfigDef.Width.MEDIUM,
                        CLOUD_ORGANIZATION_NAME_CONFIG_DISPLAY)
                .define(CLOUD_ORGANIZATION_KEY_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        CLOUD_ORGANIZATION_KEY_CONFIG_DOC,
                        SONAR_GROUP,
                        3,
                        ConfigDef.Width.MEDIUM,
                        CLOUD_ORGANIZATION_KEY_CONFIG_DISPLAY)
                .define(CLOUD_PROJECT_KEYS_CONFIG,
                        ConfigDef.Type.STRING,
                        "",
                        ConfigDef.Importance.LOW,
                        CLOUD_PROJECT_KEYS_CONFIG_DOC,
                        SONAR_GROUP,
                        5,
                        ConfigDef.Width.MEDIUM,
                        CLOUD_PROJECT_KEYS_CONFIG_DISPLAY)
                .define(CLOUD_METRIC_KEYS_CONFIG,
                        ConfigDef.Type.STRING,
                        CLOUD_METRIC_KEYS_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        CLOUD_METRIC_KEYS_CONFIG_DOC,
                        SONAR_GROUP,
                        6,
                        ConfigDef.Width.LONG,
                        CLOUD_METRIC_KEYS_CONFIG_DISPLAY)
                .define(CLOUD_MEASURE_TOPIC_CONFIG,
                        ConfigDef.Type.STRING,
                        CLOUD_MEASURE_TOPIC_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        CLOUD_MEASURE_TOPIC_CONFIG_DOC,
                        SONAR_GROUP,
                        7, ConfigDef.Width.MEDIUM,
                        CLOUD_MEASURE_TOPIC_CONFIG_DISPLAY)
                .define(CLOUD_ISSUE_TOPIC_CONFIG,
                        ConfigDef.Type.STRING, CLOUD_ISSUE_TOPIC_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW, CLOUD_ISSUE_TOPIC_CONFIG_DOC,
                        SONAR_GROUP,
                        8, ConfigDef.Width.MEDIUM,
                        CLOUD_ISSUE_TOPIC_CONFIG_DISPLAY)
                .define(CLOUD_INTERVAL_SECONDS_CONFIG,
                        ConfigDef.Type.LONG,
                        SONAR_INTERVAL_SECONDS_CONFIG_DEFAULT,
                        ConfigDef.Importance.LOW,
                        CLOUD_INTERVAL_SECONDS_CONFIG_DOC,
                        SONAR_GROUP,
                        9,
                        ConfigDef.Width.SHORT,
                        CLOUD_INTERVAL_SECONDS_CONFIG_DISPLAY)
                .define(SONAR_SNAPSHOTDATE_CONFIG,
                        ConfigDef.Type.STRING,
                        "2022-11-03",
                        ConfigDef.Importance.HIGH,
                        SONAR_SNAPSHOTDATE_CONFIG_DOC,
                        SONAR_GROUP,
                        10,
                        ConfigDef.Width.MEDIUM,
                        SONAR_SNAPSHOTDATE_CONFIG_DISPLAY);
    }

    public SonarCloudSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }


}
