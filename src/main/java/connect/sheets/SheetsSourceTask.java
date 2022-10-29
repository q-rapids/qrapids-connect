package connect.sheets;


import com.beust.ah.A;
import model.sheets.Developer;
import model.sheets.TeamInformation;
import model.sheets.TimeInputation;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

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

    private String teamName;


    private String spreadSheetId;

    private String[] developers;

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
    private void initializeConfigurations(Map<String, String> properties) {
        spreadSheetId = properties.get(SheetsSourceConfig.SPREADSHEET_ID);
        timeTopic = properties.get(SheetsSourceConfig.SHEET_HOUR_TOPIC_CONFIG);
        pollIntervalConfiguration = properties.get(SheetsSourceConfig.SHEET_INTERVAL_SECONDS_CONFIG);
        String sprintsConfig = properties.get(SheetsSourceConfig.SHEET_SPRINT_NAMES);
        sprints = sprintsConfig.split(",");
        String developersConfig = properties.get(SheetsSourceConfig.SHEET_MEMBER_NAMES);
        developers = developersConfig.split(",");
        teamName = properties.get(SheetsSourceConfig.SHEET_TEAM_NAME);
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

    private TeamInformation getTeamInformation() {
        TeamInformation teamInformation = new TeamInformation();
        teamInformation.id = UUID.randomUUID();
        teamInformation.teamName = teamName;
        teamInformation.spreadsheetId = spreadSheetId;
        teamInformation.time = String.valueOf(System.currentTimeMillis());
        return teamInformation;
    }

    private SourceRecord generateTeamRecords() throws AuthorizationCredentialsException, IOException {
        TeamInformation teamInformation = getTeamInformation();
        ArrayList<Developer> developersInformation = new ArrayList<>();
        for(int developerPosition = 0;
            developerPosition < developers.length; ++developerPosition) {
            String developerName = developers[developerPosition];
            Developer developerInformation = getDeveloperInformation(developerPosition, developerName);
            developersInformation.add(developerInformation);
        }
        teamInformation.developerInfo = developersInformation.toArray(Developer[]::new);
        return getSheetSourceRecord(teamInformation);
    }

    private Developer getDeveloperInformation(final Integer developerPosition, final String name) throws AuthorizationCredentialsException, IOException {
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
        timeInputation.sheetId = SheetsApi.getSheetId(sprintName, spreadSheetId);
        timeInputation.sprintName = sprintName;
        timeInputation.sprintHours = SheetsApi.getHoursDone(developerPosition, sprintName, spreadSheetId);
        return timeInputation;
    }

    @Override
    public String version() {
        return "0.1";
    }

    @Override
    public void start(Map<String, String> properties) {
        taskLogger.info("connect-sheets // TASK: start");
        initializeAuthorizationCredentials(properties);
        initializeConfigurations(properties);
        setPollConfiguration();

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
        }
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
            records.add(generateTeamRecords());
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

        Struct imputation = new Struct(SheetsSchema.imputationSchema);

        Struct developer = new Struct(SheetsSchema.developerSchema);

        Struct information = new Struct(SheetsSchema.sheetSchema);
        information.put(SheetsSchema.FIELD_ID, teamInformation.id.toString());
        information.put(SheetsSchema.FIELD_TEAM_NAME, teamInformation.teamName);
        information.put(SheetsSchema.FIELD_SPREADSHEET_ID, spreadSheetId);
        information.put(SheetsSchema.FIELD_TIME, teamInformation.time);
        information.put(SheetsSchema.FIELD_DEVELOPER_INFO, teamInformation.developerInfo);
        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                timeTopic,
                Schema.STRING_SCHEMA,
                teamInformation.id, //uuid as elasticsearch index
                Schema.STRING_SCHEMA,
                information);
    }

    @Override
    public void stop() {

    }
}
