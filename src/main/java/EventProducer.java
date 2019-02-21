import org.apache.kafka.clients.producer.RecordMetadata;
import util.config.ConfigFileNotFoundException;
import util.config.ConfigNotFoundException;
import util.config.ConfigUtils;
import util.kafka.KafkaEventHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class EventProducer {

    public static void main(String[] args) throws Exception {
        System.out.println("[Start program] --------------------------");
        String sessionId = UUID.randomUUID().toString();
        System.out.println(String.format("Session Id: %1s", sessionId));
        System.out.println("[Publish events] --------------------------");
        List<Result> results = publishEvents(sessionId);
        System.out.println("[Output csv] --------------------------");
        writeCsv(results);
        System.out.println("[End program] --------------------------");
    }

    private static List<Result> publishEvents(String sessionId) throws ConfigFileNotFoundException, ConfigNotFoundException, ExecutionException,
            InterruptedException {
        List<Result> results = new ArrayList<Result>();
        Properties config = ConfigUtils.build().getProperties("application.properties");
        int count = Integer.parseInt(ConfigUtils.build().getProperty(config, "publish.count"));
        String eventTopic = ConfigUtils.build().getProperty(config, "kafka.event.topic");
        KafkaEventHelper kafkaEventHelper = new KafkaEventHelper(getKafkaProducerProps());
        try {
            for (int i = 0; i < count; i++) {
                String eventContent = String.format("%1s-%2s", sessionId, i);
                Result result = new Result();
                result.index = i;
                result.beforePublishDate = new Date();
                result.eventContent = eventContent;
                RecordMetadata recordMetadata = kafkaEventHelper.sendEvent(eventTopic, eventContent);
                result.offset = recordMetadata.offset();
                result.afterPublishDate = new Date();
                result.diffMs = result.afterPublishDate.getTime() - result.beforePublishDate.getTime();
                results.add(result);
                System.out.println(String.format("[Event] Publish event to kafka: %1s", result.toString()));
            }
            return results;
        } catch (Exception e) {
            System.err.println(e.toString());
            throw e;
        } finally {
            kafkaEventHelper.close();
        }
    }

    private static void writeCsv(List<Result> results) throws ConfigFileNotFoundException, ConfigNotFoundException, IOException {
        Properties config = ConfigUtils.build().getProperties("application.properties");
        String csvPath = ConfigUtils.build().getProperty(config, "result.csv.path");
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(csvPath);
            System.out.println("Start to generate result CSV file.");
            fileWriter.write("Index, Event Offset, Event Content, Before Publish Date, After Publish Date, Time Diff Millis");
            fileWriter.write(System.lineSeparator());
            for (int i = 0; i < results.size(); i++) {
                String line = results.get(i).toString();
                System.out.println(line);
                fileWriter.write(line);
                fileWriter.write(System.lineSeparator());
                System.out.println(String.format("[CSV] Write line: %1s", line));
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            throw e;
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    private static Properties getKafkaProducerProps() throws ConfigFileNotFoundException, ConfigNotFoundException {
        Properties kafkaProducerProperties = new Properties();
        Properties properties = ConfigUtils.build().getProperties("application.properties");
        String propertyPrefix = "kafka.producer.prop.";
        for (Object key : properties.keySet()) {
            String keyString = (String) key;
            if (keyString.startsWith(propertyPrefix)) {
                kafkaProducerProperties.setProperty(keyString.replaceFirst(propertyPrefix, ""), ConfigUtils.build().getProperty(properties,
                        keyString));
            }
        }
        return kafkaProducerProperties;
    }
}

class Result {

    int index;
    long offset;
    String eventContent;
    Date beforePublishDate;
    Date afterPublishDate;
    long diffMs;

    public String toString() {
        return String.format("%1s,%2s,%3s,%4s,%5s,%6s", index, offset, eventContent, beforePublishDate, afterPublishDate, diffMs);
    }
}