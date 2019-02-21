package util.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Kafka event helper.
 */
public class KafkaEventHelper {

    private Properties configs;
    private Producer<String, String> producer = null;

    public KafkaEventHelper(Properties configs) {
        this.configs = configs;
        producer = new KafkaProducer<String, String>(configs);
    }

    public RecordMetadata sendEvent(String topic, String event) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> data = new ProducerRecord<String, String>(topic, event);
        return producer.send(data).get();
    }

    public void close() {
        producer.close();
    }
}
