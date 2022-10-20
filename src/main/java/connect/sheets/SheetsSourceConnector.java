package connect.sheets;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SheetsSourceConnector extends SourceConnector {

    private Logger sheetsLog = Logger.getLogger(SheetsSourceConnector.class.getName());

    private String filename;

    private String topic;

    @Override
    public String version() {
        return null;
    }

    @Override
    public void start(Map<String, String> props) {

    }

    @Override
    public Class<? extends Task> taskClass() {
        return SheetsSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        return new ArrayList<>();
    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return null;
    }
}
