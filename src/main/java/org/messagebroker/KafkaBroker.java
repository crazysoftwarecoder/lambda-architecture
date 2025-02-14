package org.messagebroker;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaBroker implements MessageBroker<KafkaProducer<String, String>, RecordMetadata>  {

    private final Properties adminConfig = new Properties();
    private String topic;
    private String host;
    private int port;

    public KafkaBroker(String host, int port) {
        this.host = host;
        this.port = port;
        adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, host + ":" + port);
    }

    public void createTopic(String topic, int numPartitions, short replicationFactor) throws InterruptedException, ExecutionException {
        var adminClient = AdminClient.create(adminConfig);
        this.topic = topic;
        var newTopic = new NewTopic(topic, numPartitions, replicationFactor);
        adminClient.createTopics(Arrays.asList(newTopic)).all().get();
    }

    public KafkaProducer<String, String> createProducer() {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host + ":" + port);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(producerConfig);
    }

    public RecordMetadata sendMessage(KafkaProducer<String, String> producer, String key, String value) throws InterruptedException, ExecutionException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        return producer.send(record).get();
    }

    public void closeProducer(KafkaProducer<String, String> producer) {
        producer.close();
    }
}
