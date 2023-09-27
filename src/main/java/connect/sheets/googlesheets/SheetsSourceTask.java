package connect.sheets.googlesheets;


import com.google.api.services.sheets.v4.model.ValueRange;
import connect.sheets.AuthorizationCredentials;
import connect.sheets.exceptions.AuthorizationCredentialsException;
import model.sheets.IterationInformation;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.ZoneId;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Kafka Connect SourceTask class for Google Sheets
 * @author Max Tiessler
 */
public class SheetsSourceTask extends SourceTask {

    private static final TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    private static final DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    static { dfZULU.setTimeZone(tzUTC); }

    private String pollIntervalConfiguration;

    private Integer pollInterval;

    private String teamsNumConfiguration;

    private Integer teamsNum;

    private String teamsIntervalConfiguration;

    private Integer teamsInterval;

    private Long lastPollTime = 0L;

    private List<String> spreadSheetId;

    private List<String> teamName;

    private String[] sprints;

    private List<String> sheetsImputationTopic;

    private String teamId;

    private int currentTaskID;

    private AuthorizationCredentials authorizationCredentials;

    private final Logger taskLogger = LoggerFactory.getLogger(SheetsSourceTask.class.getName());

    private void initializeAuthorizationCredentials(final Map<String, String> properties) {
        taskLogger.info("connect-sheets // TASK: Initialize Authorization credentials");
        authorizationCredentials = AuthorizationCredentials.getInstance(properties);
    }
    private void initializeConfigurations(final Map<String, String> properties) {

        String sprintsConfig = properties.get(SheetsSourceConfig.SHEETS_SPRINT_NAMES);
        if (sprintsConfig == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_SPRINT_NAMES), ""))
            throw new ConnectException("SheetsConnector configuration must include 'sprint.names' setting");
        sprints = sprintsConfig.split(",");

        teamsNumConfiguration = properties.get(SheetsSourceConfig.SHEETS_TEAMS_NUMBER_CONFIG);
        if (teamsNumConfiguration == null || Objects.equals( properties.get(SheetsSourceConfig.SHEETS_TEAMS_NUMBER_CONFIG), "") )
            throw new ConnectException("SheetsConnector configuration must include 'sheets.teams.num' setting");

        pollIntervalConfiguration = properties.get(SheetsSourceConfig.SHEETS_INTERVAL_SECONDS_CONFIG);
        teamsIntervalConfiguration = properties.get(SheetsSourceConfig.SHEETS_TEAMS_INTERVAL_CONFIG);
        teamId = UUID.randomUUID().toString();
        currentTaskID = 0;

        spreadSheetId = new ArrayList<>();
        teamName = new ArrayList<>();
        sheetsImputationTopic = new ArrayList<>();

        teamsNum = Integer.parseInt(teamsNumConfiguration);
        for (int i = 0; i < teamsNum; ++i) {

            String spreadSheetId = properties.get("tasks." + i + "." + SheetsSourceConfig.SPREADSHEETS_ID);
            if (spreadSheetId == null || Objects.equals(properties.get(SheetsSourceConfig.SPREADSHEETS_ID), ""))
                throw new ConnectException("SheetsConnector configuration must include 'tasks."
                + i + ".spreadsheet.id' setting");
            this.spreadSheetId.add(spreadSheetId);

            String teamName = properties.get("tasks." + i + "." + SheetsSourceConfig.SHEETS_TEAM_NAME);
            if (teamName == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_TEAM_NAME), ""))
                throw new ConnectException("SheetsConnector configuration must include 'tasks." +
                i + ".team.name' setting");
            this.teamName.add(teamName);

            String sheetsImputationTopic = properties.get("tasks." + i + "." + SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG);
            if (sheetsImputationTopic == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG), ""))
                throw new ConnectException("SheetsConnector configuration must include 'tasks." +
                i + ".imputations.topic' setting");
            this.sheetsImputationTopic.add(sheetsImputationTopic);

        }

        if (teamsNum == 0)
            throw new ConnectException("SheetsConnector configuration 'sheets.teams.num' must be bigger than 0");
    }

    private void setPollConfiguration() {
        if(pollIntervalConfiguration == null || pollIntervalConfiguration.isEmpty()) pollInterval = 3600;
        else pollInterval = Integer.parseInt(pollIntervalConfiguration);
    }

    private void setTeamIntervalConfiguration() {
        if(teamsIntervalConfiguration == null || teamsIntervalConfiguration.isEmpty()) teamsInterval = 120;
        else teamsInterval = Integer.parseInt(teamsIntervalConfiguration);
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

    private IterationInformation generateImputationInformation(final String sprintName,
                                                               final List<Object> information) {
        Date in = new Date();
        LocalDateTime today = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
        Date todayDate = Date.from(today.atZone(ZoneId.systemDefault()).toInstant());
        Double tth = information.get(1).toString().contains(",") ? Double.parseDouble(information.get(1).toString().replace(",", ".")) : Double.parseDouble(information.get(1).toString());
        Double reh = information.get(2).toString().contains(",") ? Double.parseDouble(information.get(2).toString().replace(",", ".")) : Double.parseDouble(information.get(2).toString());
        Double rfh = information.get(3).toString().contains(",") ? Double.parseDouble(information.get(3).toString().replace(",", ".")) : Double.parseDouble(information.get(3).toString());
        Double cph = information.get(4).toString().contains(",") ? Double.parseDouble(information.get(4).toString().replace(",", ".")) : Double.parseDouble(information.get(4).toString());
        Double fh = information.get(5).toString().contains(",") ? Double.parseDouble(information.get(5).toString().replace(",", ".")) : Double.parseDouble(information.get(5).toString());
        Double dh = information.get(6).toString().contains(",") ? Double.parseDouble(information.get(6).toString().replace(",", ".")) : Double.parseDouble(information.get(6).toString());
        Double gph = information.get(7).toString().contains(",") ? Double.parseDouble(information.get(7).toString().replace(",", ".")) : Double.parseDouble(information.get(7).toString());
        Double doh = information.get(8).toString().contains(",") ? Double.parseDouble(information.get(8).toString().replace(",", ".")) : Double.parseDouble(information.get(8).toString());
        Double ph = information.get(9).toString().contains(",") ? Double.parseDouble(information.get(9).toString().replace(",", ".")) : Double.parseDouble(information.get(9).toString());
        Double ah = information.get(10).toString().contains(",") ? Double.parseDouble(information.get(10).toString().replace(",", ".")) : Double.parseDouble(information.get(10).toString());
        return new IterationInformation(teamId)
                .teamName(teamName.get(currentTaskID))
                .spreadsheetId(spreadSheetId.get(currentTaskID))
                .timestamp(dfZULU.format(todayDate))
                .sprintName(sprintName)
                .developerName((String) information.get(0))
                .totalHours(tth)
                .reHours(reh)
                .rfHours(rfh)
                .cpHours(cph)
                .fHours(fh)
                .desHours(dh)
                .gpHours(gph)
                .docHours(doh)
                .presHours(ph)
                .altHours(ah);
    }
    private List<IterationInformation> generateImputationInformations(final List<ValueRange> googleSheetsData) {
        ArrayList<IterationInformation> iterationInformations = new ArrayList<>();
        for (int i = 0; i < googleSheetsData.size(); i += 2) {
            String sprintName = String.valueOf(googleSheetsData.get(i).getValues().get(0))
                    .substring(1, googleSheetsData.get(i).getValues().get(0).toString().length() - 1);
            ValueRange sprintInformationRange = googleSheetsData.get(i + 1);
            for (List<Object> memberIterationInformation : sprintInformationRange.getValues()) {
                iterationInformations.add(generateImputationInformation(sprintName, memberIterationInformation));
            }
        }
        return iterationInformations;
    }

    private List<IterationInformation> getTimeImputations() throws AuthorizationCredentialsException, IOException {
        List<ValueRange> googleSheetsData = SheetsApi.getMembersTotalHours(sprints, spreadSheetId.get(currentTaskID));
        return generateImputationInformations(googleSheetsData);
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
        initializeAuthorizationCredentials(properties);
        initializeConfigurations(properties);
        setPollConfiguration();
        setTeamIntervalConfiguration();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List <SourceRecord> records = new ArrayList<>();
        String messageTaskPollInfo = "lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPollTime) + " interval:" + pollInterval;
        taskLogger.info("Task Poll {}", messageTaskPollInfo);

        if (lastPollTime != 0L &&
            System.currentTimeMillis() < ( lastPollTime + ( pollInterval * 1000 ) )
            && currentTaskID == 0) {

            taskLogger.info("------- exit polling, " + (System.currentTimeMillis() - lastPollTime) / 1000L + " secs since last poll.");
            Thread.sleep(1000L);
        }

        else {
            if (currentTaskID == 0) lastPollTime = System.currentTimeMillis();
            Thread.sleep(teamsInterval * 1000L); // Wait between teams
            taskLogger.info("\n\n\n**********");
            taskLogger.info( "Start executing task " + currentTaskID + " with team name " + teamName.get(currentTaskID));

            try {
                records = getTimeImputations()
                    .stream()
                    .parallel()
                    .map(this::getSheetSourceRecord)
                    .collect(Collectors.toList());
            }
            catch (AuthorizationCredentialsException | IOException e) {
                throw new RuntimeException(e);
            }

            taskLogger.info("Records: {}", records);
            taskLogger.info("Finished executing task " + currentTaskID + " with team name " + teamName.get(currentTaskID));
            taskLogger.info("**********\n\n\n");
            ++currentTaskID;
            if (currentTaskID == teamsNum) currentTaskID = 0;
        }

        return records;
    }


    private SourceRecord getSheetSourceRecord(IterationInformation iterationInformation) {

        Map<String,String> sourcePartition = new HashMap<>();
        sourcePartition.put("spreadsheetId", iterationInformation.spreadsheetId());

        Map<String,String> sourceOffset = new HashMap<>();
        sourceOffset.put("updated", dfZULU.format(new Date(System.currentTimeMillis())));

        Struct imputationSchema = new Struct(SheetsSchema.sheetsImputationSchema);
        imputationSchema.put(SheetsSchema.TEAM_ID, iterationInformation.id());
        imputationSchema.put(SheetsSchema.TEAM_NAME, iterationInformation.teamName());
        imputationSchema.put(SheetsSchema.SPREADSHEET_ID, iterationInformation.spreadsheetId());
        imputationSchema.put(SheetsSchema.TIMESTAMP, iterationInformation.timestamp());
        imputationSchema.put(SheetsSchema.DEVELOPER_NAME, iterationInformation.developerName());
        imputationSchema.put(SheetsSchema.SPRINT_NAME, iterationInformation.sprintName());
        imputationSchema.put(SheetsSchema.TOTAL_HOURS, iterationInformation.totalHours());
        imputationSchema.put(SheetsSchema.RE_HOURS, iterationInformation.reHours());
        imputationSchema.put(SheetsSchema.RF_HOURS, iterationInformation.rfHours());
        imputationSchema.put(SheetsSchema.CP_HOURS, iterationInformation.cpHours());
        imputationSchema.put(SheetsSchema.F_HOURS, iterationInformation.fHours());
        imputationSchema.put(SheetsSchema.DES_HOURS, iterationInformation.desHours());
        imputationSchema.put(SheetsSchema.GP_HOURS, iterationInformation.gpHours());
        imputationSchema.put(SheetsSchema.DOC_HOURS, iterationInformation.docHours());
        imputationSchema.put(SheetsSchema.PRES_HOURS, iterationInformation.presHours());
        imputationSchema.put(SheetsSchema.ALT_HOURS, iterationInformation.altHours());

        String key = iterationInformation.sprintName() + " - " + iterationInformation.developerName();

        return new SourceRecord(sourcePartition,
                sourceOffset,
                sheetsImputationTopic.get(currentTaskID),
                Schema.STRING_SCHEMA,
                key,
                SheetsSchema.sheetsImputationSchema,
                imputationSchema);

    }

    @Override
    public void stop() {

    }
}
