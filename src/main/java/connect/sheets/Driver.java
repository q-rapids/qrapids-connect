package connect.sheets;

import connect.sheets.googlesheets.SheetsSourceTask;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.LoggerFactory;
import util.PropertyFile;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the SheetsSourceTask without connector
 * @author Max Tiessler
 */

public class Driver {

    private static final Logger driverLogger = LoggerFactory.getLogger(Driver.class.getName());
    public static void main(String[] args) throws InterruptedException {
        Map<String, String> config = PropertyFile.get("./config/connect-sheets.properties");
        SheetsSourceTask sheetsSourceTask = new SheetsSourceTask();
        sheetsSourceTask.start(config);
        while (true) {
            List<SourceRecord> records = sheetsSourceTask.poll();
            for (SourceRecord sr : records) {
                String sourceRecordResult = sr.toString();
                driverLogger.info("Result: {}", sourceRecordResult);
            }
        }
    }
}