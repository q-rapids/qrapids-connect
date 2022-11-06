package connect.sheets;


import model.sheets.Developer;
import model.sheets.TeamInformation;
import model.sheets.TimeInputation;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private String[] spreadSheetIds;

    private String actualSpreadSheet;

    private String[] numberDevelopersPerTeam;

    private String[] teamNames;

    private String[] sprints;
    private String timeTopic;

    private AuthorizationCredentials authorizationCredentials;
    private final Logger taskLogger = Logger.getLogger(SheetsSourceTask.class.getName());

    private void initializeAuthorizationCredentials(final Map<String, String> properties) {
        taskLogger.info("connect-sheets // TASK: Initialize Authorization credentials");
        authorizationCredentials = AuthorizationCredentials.getInstance(
                properties.get(SheetsSourceConfig.SHEET_TYPE),
                properties.get(SheetsSourceConfig.SHEET_PROJECT_ID),
                properties.get(SheetsSourceConfig.SHEET_PRIVATE_KEY_ID),
                properties.get(SheetsSourceConfig.SHEET_PRIVATE_KEY),
                properties.get(SheetsSourceConfig.SHEET_CLIENT_EMAIL),
                properties.get(SheetsSourceConfig.SHEET_CLIENT_ID),
                properties.get(SheetsSourceConfig.SHEET_AUTH_URI),
                properties.get(SheetsSourceConfig.SHEET_TOKEN_URI),
                properties.get(SheetsSourceConfig.SHEET_AUTH_PROVIDER_URL),
                properties.get(SheetsSourceConfig.SHEET_CLIENT_CERTIFICATION_URL));
    }
    private void initializeConfigurations(final Map<String, String> properties) {
        timeTopic = properties.get(SheetsSourceConfig.SHEET_HOUR_TOPIC_CONFIG);
        pollIntervalConfiguration = properties.get(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG);
        String spreadSheetIdsAux = properties.get(SheetsSourceConfig.SPREADSHEET_IDS);
        spreadSheetIds = spreadSheetIdsAux.split(",");
        String sprintsConfig = properties.get(SheetsSourceConfig.SHEET_SPRINT_NAMES);
        sprints = sprintsConfig.split(",");
        String memberNumber = properties.get(SheetsSourceConfig.SHEET_TEAM_NUMBER_MEMBERS);
        numberDevelopersPerTeam = memberNumber.split(",");
        String teamNamesAux = properties.get(SheetsSourceConfig.SHEET_TEAM_NAMES);
        teamNames = teamNamesAux.split(",");

    }
    private void setPollConfiguration() {
        lastPollTime = 0L;
        if(pollIntervalConfiguration == null || pollIntervalConfiguration.isEmpty()) {
            pollInterval = 3600;
        } else{
            //pollInterval = Integer.parseInt(pollIntervalConfiguration);
            pollInterval = 60*60;
        }
    }

    private boolean lostConnection() {
        return System.currentTimeMillis() < (lastPollTime + (1000));
    }

    private TeamInformation getTeamInformation(final String name) {
        TeamInformation teamInformation = new TeamInformation();
        teamInformation.id = UUID.randomUUID();
        teamInformation.teamName = name;
        teamInformation.spreadsheetId = actualSpreadSheet;
        teamInformation.time = String.valueOf(System.currentTimeMillis());
        return teamInformation;
    }

    private SourceRecord generateTeamRecords(final String name, final Integer teamNumber) throws AuthorizationCredentialsException, IOException {
        TeamInformation teamInformation = getTeamInformation(name);
        ArrayList<Developer> developersInformation = new ArrayList<>();
        for(int developerPosition = 0;
            developerPosition < Integer.parseInt(numberDevelopersPerTeam[teamNumber]); ++developerPosition) {
            String developerName = SheetsApi.getDeveloperName(developerPosition, actualSpreadSheet);
            Developer developerInformation = getDeveloperInformation(developerPosition, developerName);
            developersInformation.add(developerInformation);
        }
        teamInformation.developerInfo = developersInformation.toArray(Developer[]::new);
        return getSheetSourceRecord(teamInformation);
    }

    private Developer getDeveloperInformation(final Integer developerPosition,
                                              final String name)
                                              throws AuthorizationCredentialsException, IOException {
        Developer developer = new Developer();
        developer.developerName = name;
        ArrayList<TimeInputation> developerTimeImputations = new ArrayList<>();
        for (String sprint : sprints) {
            TimeInputation developerTimeImputation = getDeveloperTimeImputation(developerPosition, sprint);
            developerTimeImputations.add(developerTimeImputation);
        }
        developer.timeInputations = developerTimeImputations.toArray(TimeInputation[]::new);
        return developer;
    }

    private TimeInputation getDeveloperTimeImputation(final Integer developerPosition,
                                                      final String sprintName) throws AuthorizationCredentialsException, IOException {
        TimeInputation timeInputation = new TimeInputation();
        timeInputation.sheetId = SheetsApi.getSheetId(sprintName, actualSpreadSheet);
        timeInputation.sprintName = sprintName;
        timeInputation.sprintHours = SheetsApi.getHoursDone(developerPosition, sprintName, actualSpreadSheet);
        return timeInputation;
    }

    /**
     * Version getter
     * @return connector version
     */
    @Override
    public String version() {
        return "0.1";
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
        /*
        Map<String,String> sourcePartition = new HashMap<>();
        //sourcePartition.put( "githubUrl", githubUrls[0] );
        if (context != null) {
            Map<String,Object> offset = context.offsetStorageReader().offset(sourcePartition);
            if (offset != null ) {
                try {
                    lastDataRetreived = dfZULU.parse( (String) offset.get("updated") );
                    taskLogger.info("--------------------------" + "found offset: updated=" + lastDataRetreived);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }*/
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List <SourceRecord> records = new ArrayList<>();
        taskLogger.info("lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPollTime)
                + " interval:" + pollIntervalConfiguration);
        if(lastPollTime != 0 && lostConnection()) {
            Thread.sleep(1000);
            return records;
        }
        lastPollTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < teamNames.length; ++i) {
                String team = teamNames[i];
                Integer teamNumber = i;
                actualSpreadSheet = spreadSheetIds[i];
                SourceRecord teamRecord = generateTeamRecords(team, teamNumber);
                System.out.println(teamRecord);
                records.add(teamRecord);
            }
        } catch (AuthorizationCredentialsException | IOException e) {
            throw new RuntimeException(e);
        }
        taskLogger.info("connect-sheets // TASK: Finished task");

        return records;

    }


    private SourceRecord getSheetSourceRecord(TeamInformation teamInformation) {
        Map<String, String> sourcePartition = new HashMap<>();
        sourcePartition.put("id", teamInformation.id.toString());
        sourcePartition.put("spreadsheet_id", teamInformation.spreadsheetId);
        Map<String, String> sourceOffset = new HashMap<>();
        sourceOffset.put("created", dfZULU.format(new Date(System.currentTimeMillis())));

        Struct teamData = new Struct(SheetsSchema.sheetSchema);
        teamData.put(SheetsSchema.FIELD_ID, teamInformation.id.toString());
        teamData.put(SheetsSchema.FIELD_TEAM_NAME, teamInformation.teamName);
        teamData.put(SheetsSchema.FIELD_SPREADSHEET_ID, actualSpreadSheet);
        teamData.put(SheetsSchema.FIELD_TIME, teamInformation.time);
        Vector<Struct> developers = new Vector<>();
        for (Developer developerInfo : teamInformation.developerInfo) {
            Struct developerData = new Struct(SheetsSchema.developer);
            developerData.put(SheetsSchema.FIELD_DEVELOPER_NAME, developerInfo.developerName);
            Vector<Struct> developerImputations = new Vector<>();
            for (TimeInputation timeInputation : developerInfo.timeInputations) {
                Struct imputation = new Struct(SheetsSchema.imputationSchema);
                imputation.put(SheetsSchema.FIELD_SHEET_ID, timeInputation.sheetId);
                imputation.put(SheetsSchema.FIELD_SPRINT_NAME, timeInputation.sprintName);
                imputation.put(SheetsSchema.FIELD_DEVELOPER_TIME, String.valueOf(timeInputation.sprintHours));
                developerImputations.add(imputation);
            }
            developerData.put(SheetsSchema.FIELD_IMPUTATION_TIMES, developerImputations);
            developers.add(developerData);
        }
        teamData.put(SheetsSchema.FIELD_DEVELOPER_INFO, developers);
        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                timeTopic,
                Schema.STRING_SCHEMA,
                teamInformation.id, //uuid as elasticsearch index
                Schema.STRING_SCHEMA,
                teamData);
    }

    @Override
    public void stop() {

    }
}
