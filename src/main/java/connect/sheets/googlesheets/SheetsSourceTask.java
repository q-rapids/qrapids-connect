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
    static {
        dfZULU.setTimeZone(tzUTC);
    }
    private String pollIntervalConfiguration;

    private Integer pollInterval;

    private Long lastPollTime = 0L;

    private String spreadSheetId;

    private String teamName;

    private String[] sprints;
    private String sheetsImputationTopic;

    private String teamId;

    private AuthorizationCredentials authorizationCredentials;
    private final Logger taskLogger = LoggerFactory.getLogger(SheetsSourceTask.class.getName());

    private void initializeAuthorizationCredentials(final Map<String, String> properties) {
        taskLogger.info("connect-sheets // TASK: Initialize Authorization credentials");
        authorizationCredentials = AuthorizationCredentials.getInstance(properties);
    }
    private void initializeConfigurations(final Map<String, String> properties) {
        spreadSheetId = properties.get(SheetsSourceConfig.SPREADSHEETS_ID);
        if (spreadSheetId == null || Objects.equals(properties.get(SheetsSourceConfig.SPREADSHEETS_ID), "")) {
            throw new ConnectException("SheetsConnector configuration must include spreadsheet.ids setting");
        }

        String sprintsConfig = properties.get(SheetsSourceConfig.SHEETS_SPRINT_NAMES);
        if (sprintsConfig == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_SPRINT_NAMES), "")) {
            throw new ConnectException("SheetsConnector configuration must include sprint.names setting");
        }

        sprints = sprintsConfig.split(",");

        teamName = properties.get(SheetsSourceConfig.SHEETS_TEAM_NAME);
        if (teamName == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_TEAM_NAME), "")) {
            throw new ConnectException("SheetsConnector configuration must include team.name setting");
        }

        sheetsImputationTopic = properties.get(SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG);
        if (sheetsImputationTopic == null || Objects.equals(properties.get(SheetsSourceConfig.SHEETS_IMPUTATIONS_TOPIC_CONFIG), "")) {
            throw new ConnectException("SheetsConnector configuration must include imputations.topic setting");
        }

        pollIntervalConfiguration = properties.get(SheetsSourceConfig.SHEETS_INTERVAL_SECONDS_CONFIG);

        teamId = UUID.randomUUID().toString();
    }
    private void setPollConfiguration() {

        if(pollIntervalConfiguration == null || pollIntervalConfiguration.isEmpty()) {
            pollInterval = 3600;
        } else{
            pollInterval = Integer.parseInt(pollIntervalConfiguration);
        }
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
        return new IterationInformation(teamId)
                .teamName(teamName)
                .spreadsheetId(spreadSheetId)
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
                .presHours(ph);
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
        List<ValueRange> googleSheetsData = SheetsApi.getMembersTotalHours(sprints, spreadSheetId);
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
        taskLogger.info("connect-sheets // TASK: start");
        initializeAuthorizationCredentials(properties);
        initializeConfigurations(properties);
        setPollConfiguration();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List <SourceRecord> records = new ArrayList<>();
        String messageTaskPollInfo = "lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPollTime)
                + " interval:" + pollInterval;
        taskLogger.info("Task Poll {}", messageTaskPollInfo);
        if (lastPollTime != 0) {
            if (System.currentTimeMillis() < ( lastPollTime + (pollInterval * 1000))) {
                Thread.sleep(1000);
                return records;
            }
        }
        lastPollTime = System.currentTimeMillis();
        try {

            records = getTimeImputations()
                    .stream()
                    .parallel()
                    .map(this::getSheetSourceRecord)
                    .collect(Collectors.toList());
        } catch (AuthorizationCredentialsException | IOException e) {
            throw new RuntimeException(e);
        }
        taskLogger.info("Records: {}", records);
        taskLogger.info("connect-sheets // TASK: Finished task");
        return records;
    }


    private SourceRecord getSheetSourceRecord(IterationInformation iterationInformation) {

        Map<String,String> sourcePartition = new HashMap<>();
        sourcePartition.put("spreadsheetId", iterationInformation.spreadsheetId());

        Map<String,String> sourceOffset = new HashMap<>();
        sourceOffset.put("created", dfZULU.format(new Date(System.currentTimeMillis())));

        Struct imputationSchema = new Struct(SheetsSchema.sheetsInputationSchema);
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

        String key = iterationInformation.sprintName() + " - " + iterationInformation.developerName();

        return new SourceRecord(sourcePartition,
                sourceOffset,
                sheetsImputationTopic,
                Schema.STRING_SCHEMA,
                key,
                SheetsSchema.sheetsInputationSchema,
                imputationSchema);

    }

    @Override
    public void stop() {

    }
}
