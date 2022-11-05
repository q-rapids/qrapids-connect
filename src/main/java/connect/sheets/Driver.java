package connect.sheets;

import org.apache.kafka.connect.source.SourceRecord;
import util.PropertyFile;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs the SheetsSourceTask without connector
 * @author Max Tiessler
 */
public class Driver {
    public static void main(String[] args) throws InterruptedException {
        Logger driverLogger = Logger.getLogger(Driver.class.getName());
        Map<String, String> config = PropertyFile.get("./config/connect-sheets.properties");
        SheetsSourceTask sheetsSourceTask = new SheetsSourceTask();
        sheetsSourceTask.start(config);

        while (true) {
            List<SourceRecord> records = sheetsSourceTask.poll();
            for (SourceRecord sr : records) {
                String sourceRecordResult = sr.toString();
                driverLogger.log(Level.INFO, "Result: {}", sourceRecordResult);
            }
        }
    }
}
