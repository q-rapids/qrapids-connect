package connect.sheets.googlesheets;


import com.google.api.services.sheets.v4.model.ValueRange;
import connect.sheets.AuthorizationCredentials;
import connect.sheets.exceptions.AuthorizationCredentialsException;
import model.sheets.Developer;
import model.sheets.TeamInformation;
import model.sheets.TimeInputation;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Kafka Connect SourceTask class for Google Sheets
 * @author Max Tiessler
 */
public class SheetsSourceTask extends SourceTask {

    private static final TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    private static final DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    static {
        dfZULU.setTimeZone(tzUTC);
    }
    private String pollIntervalConfiguration;

    private Integer pollInterval;

    private Long lastPollTime;

    private Date lastDataRetreived;

    private String spreadSheetId;

    private String teamName;

    private String[] sprints;
    private String sheetHourTopic;

    private AuthorizationCredentials authorizationCredentials;
    private final Logger taskLogger = LoggerFactory.getLogger(SheetsSourceTask.class.getName());

    private void initializeAuthorizationCredentials(final Map<String, String> properties) {
        taskLogger.info("connect-sheets // TASK: Initialize Authorization credentials");
        authorizationCredentials = AuthorizationCredentials.getInstance(properties);
    }
    private void initializeConfigurations(final Map<String, String> properties) {
        pollIntervalConfiguration = properties.get(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG);

        spreadSheetId = properties.get(SheetsSourceConfig.SPREADSHEET_ID);
        if (spreadSheetId == null || Objects.equals(properties.get(SheetsSourceConfig.SPREADSHEET_ID), "")) {
            throw new ConnectException("SheetsConnector configuration must include spreadsheet.ids setting");
        }

        String sprintsConfig = properties.get(SheetsSourceConfig.SHEET_SPRINT_NAMES);
        if (sprintsConfig == null || Objects.equals(properties.get(SheetsSourceConfig.SHEET_SPRINT_NAMES), "")) {
            throw new ConnectException("SheetsConnector configuration must include sprint.names setting");
        }

        sprints = sprintsConfig.split(",");

        teamName = properties.get(SheetsSourceConfig.SHEET_TEAM_NAME);
        if (teamName == null || Objects.equals(properties.get(SheetsSourceConfig.SHEET_TEAM_NAME), "")) {
            throw new ConnectException("SheetsConnector configuration must include team.name setting");
        }

        sheetHourTopic = properties.get(SheetsSourceConfig.SHEET_HOUR_TOPIC_CONFIG);
        if (sheetHourTopic == null || Objects.equals(properties.get(SheetsSourceConfig.SHEET_HOUR_TOPIC_CONFIG), "")) {
            throw new ConnectException("SheetsConnector configuration must include hours.topic setting");
        }



    }
    private void setPollConfiguration() {
        lastPollTime = 0L;
        if(pollIntervalConfiguration == null || pollIntervalConfiguration.isEmpty()) {
            pollInterval = 3600;
        } else{
            pollInterval = Integer.parseInt(pollIntervalConfiguration);
        }
    }

    private boolean lostConnection() {
        return System.currentTimeMillis() < (lastPollTime + (1000));
    }

    private TeamInformation getTeamInformation(final String name) {
        TeamInformation teamInformation = new TeamInformation();
        teamInformation.id = UUID.randomUUID();
        teamInformation.teamName = name;
        teamInformation.spreadsheetId = spreadSheetId;
        teamInformation.time = String.valueOf(System.currentTimeMillis());
        return teamInformation;
    }

    private String getDeveloperName(final Object memberValue) {
        String auxMemberValue = memberValue.toString();
        auxMemberValue = auxMemberValue
                .substring(1, auxMemberValue.length() - 1);
        return auxMemberValue.split(",")[0];
    }

    private Boolean hasDecimalNumber(final String cellValues) {
        return cellValues.split(",").length == 3;
    }
    private Double getDeveloperHours(final Object memberValue) {
        String auxMemberValue = memberValue.toString();
        auxMemberValue = auxMemberValue
                .substring(1, auxMemberValue.length() - 1);
        if (Boolean.FALSE.equals(hasDecimalNumber(auxMemberValue))) {
            auxMemberValue = auxMemberValue.split(",")[1];
            auxMemberValue = String.valueOf(auxMemberValue.charAt(1));
        } else {
            auxMemberValue = auxMemberValue.split(", ")[1];
            auxMemberValue = auxMemberValue.replace(',', '.');
        }
        return Double.parseDouble(auxMemberValue);
    }
    private SourceRecord generateTeamRecords(final String name) throws AuthorizationCredentialsException, IOException {
        TeamInformation teamInformation = getTeamInformation(name);
        List<ValueRange> googleSheetsData = SheetsApi.getMembersTotalHours(sprints, spreadSheetId);

        Developer[] devs = getDeveloperData(googleSheetsData);
        teamInformation.developerInfo = devs;
        return getSheetSourceRecord(teamInformation);
    }

    private Developer[] getDeveloperData(final List<ValueRange> membersTotalHours) {
        Map<String, ArrayList<Double>> memberHours = new HashMap<>();
        for (ValueRange sprintValues : membersTotalHours) {
            for (Object memberValue : sprintValues.getValues()) {
                String developerName = getDeveloperName(memberValue);
                Double developerHours = getDeveloperHours(memberValue);
                if (memberHours.get(developerName) == null) {
                    memberHours.put(developerName, new ArrayList<>(Collections.singleton(developerHours)));
                } else {
                    memberHours.get(developerName).add(developerHours);
                }
            }
        }
        ArrayList<Developer> teamDevelopers = new ArrayList<>();
        for (String devName : memberHours.keySet()) {
            Developer dev = new Developer();
            dev.developerName = devName;
            ArrayList<TimeInputation> developerTimeInputations = new ArrayList<>();
            for (int i = 0; i < memberHours.get(devName).size(); ++i) {
                TimeInputation developerSprintTimeInputation = new TimeInputation();
                developerSprintTimeInputation.sprintName = sprints[i];
                developerSprintTimeInputation.sprintHours = (double) memberHours.get(devName).get(i);
                developerTimeInputations.add(developerSprintTimeInputation);
            }
            TimeInputation[] times = new TimeInputation[developerTimeInputations.size()];
            for (int i = 0; i < developerTimeInputations.size(); ++i) {
                times[i] = developerTimeInputations.get(i);
            }
            dev.timeInputations = times;
            teamDevelopers.add(dev);
        }
        Developer[] devs = new Developer[teamDevelopers.size()];
        for (int i = 0; i < teamDevelopers.size(); ++i) {
            devs[i] = teamDevelopers.get(i);
        }
        return devs;
    }

    /**
     * Version getter
     * @return connector version
     */
    @Override
    public String version() {
        return "0.5";
    }

    /**
     * Initializes the task
     * @param properties    Task properties
     */
    @Override
    public void start(Map<String, String> properties) {
        taskLogger.info("connect-sheets // TASK: start");
        initializeAuthorizationCredentials(properties);
        initializeConfigurations(properties);
        setPollConfiguration();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List <SourceRecord> records = new ArrayList<>();
        String messageTaskPollInfo = "lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPollTime)
                + " interval:" + pollIntervalConfiguration;
        taskLogger.info("Task Poll {}", messageTaskPollInfo);
        if (lastPollTime != 0) {
            if (System.currentTimeMillis() < ( lastPollTime + (3600 * 1000))) {
                Thread.sleep(342433);
                return records;
            }
        }
        lastPollTime = System.currentTimeMillis();
        try {
            SourceRecord teamRecord = generateTeamRecords(teamName);
            records.add(teamRecord);
            taskLogger.info("TeamRecord {}", teamRecord);
        } catch (AuthorizationCredentialsException | IOException e) {
            throw new RuntimeException(e);
        }
        taskLogger.info("connect-sheets // TASK: Finished task");
        return records;
    }


    private SourceRecord getSheetSourceRecord(TeamInformation teamInformation) {

        Map<String,String> m = new HashMap<>();
        m.put("2", "2");

        Struct teamData = new Struct(SheetsSchema.sheetSchema);
        teamData.put(SheetsSchema.TEAM_ID, teamInformation.id.toString());
        teamData.put(SheetsSchema.FIELD_TEAM_NAME, teamInformation.teamName);
        teamData.put(SheetsSchema.FIELD_SPREADSHEET_ID, spreadSheetId);
        teamData.put(SheetsSchema.FIELD_TIME, teamInformation.time);
        Vector<Struct> developers = new Vector<>();
        for (Developer developerInfo : teamInformation.developerInfo) {
            Struct developerData = new Struct(SheetsSchema.developer);
            developerData.put(SheetsSchema.FIELD_DEVELOPER_NAME, developerInfo.developerName);
            Vector<Struct> developerImputations = new Vector<>();
            for (TimeInputation timeInputation : developerInfo.timeInputations) {
                Struct imputation = new Struct(SheetsSchema.imputationSchema);
                imputation.put(SheetsSchema.FIELD_SPRINT_NAME, timeInputation.sprintName);
                imputation.put(SheetsSchema.FIELD_DEVELOPER_TIME, timeInputation.sprintHours.floatValue());
                developerImputations.add(imputation);
            }
            developerData.put(SheetsSchema.FIELD_IMPUTATION_TIMES, developerImputations);
            developers.add(developerData);
        }
        teamData.put(SheetsSchema.FIELD_DEVELOPER_INFO, developers);

        return new SourceRecord(m, m, sheetHourTopic, SheetsSchema.sheetSchema, teamData);

    }

    @Override
    public void stop() {

    }
}
