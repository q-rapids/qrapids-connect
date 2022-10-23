package connect.sheets;

import org.apache.kafka.connect.source.SourceRecord;
import util.PropertyFile;

import java.util.List;
import java.util.Map;

public class Driver {
    public static void main(String[] args) throws InterruptedException {

        Map<String, String> config = PropertyFile.get("./config/connect-sheets.properties");
        SheetsSourceTask sheetsSourceTask = new SheetsSourceTask();
        sheetsSourceTask.start(config);

        while (true) {
            List<SourceRecord> records = sheetsSourceTask.poll();
            for (SourceRecord sr : records) {
                System.out.println(sr);
            }
        }
    }
}
