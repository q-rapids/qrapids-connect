package connect.sheets;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SheetsSourceTask extends SourceTask {
    @Override
    public String version() {
        return null;
    }

    @Override
    public void start(Map<String, String> map) {

    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return new ArrayList<>();
    }

    @Override
    public void stop() {

    }
}
