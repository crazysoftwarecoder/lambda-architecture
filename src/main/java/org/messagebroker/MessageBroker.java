package org.messagebroker;

import java.util.concurrent.ExecutionException;
public interface MessageBroker<Producer, MessageMetadata> {
    void createTopic(String topic, int numPartitions, short replicationFactor) throws InterruptedException, ExecutionException;
    Producer createProducer();
    MessageMetadata sendMessage(Producer producer, String key, String value) throws InterruptedException, ExecutionException;
    void closeProducer(Producer producer);
}
