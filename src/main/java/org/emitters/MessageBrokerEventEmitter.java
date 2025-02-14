package org.emitters;

import org.messagebroker.MessageBroker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class MessageBrokerEventEmitter {

    private static final Logger logger = Logger.getLogger(MessageBrokerEventEmitter.class.getName());
    
    private RandomEventCreator<EventType> eventCreator;
    private MessageBroker<KafkaProducer<String, String>, RecordMetadata> messageBroker;
    private int emitEveryNthSecond;
    private KafkaProducer<String, String> producer;
    private ScheduledExecutorService executor;

    public MessageBrokerEventEmitter(RandomEventCreator<EventType> eventCreator, MessageBroker<KafkaProducer<String, String>, RecordMetadata> messageBroker, int emitEveryNthSecond) {
        this.eventCreator = eventCreator;
        this.messageBroker = messageBroker;
        this.emitEveryNthSecond = emitEveryNthSecond;
    }

    public void emitEvents() {
        producer = messageBroker.createProducer();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                var events = eventCreator.emitEvents();
                for (var event : events.entrySet()) {
                    logger.info("Emitting event: " + event.getKey() + " " + event.getValue());
                    messageBroker.sendMessage(producer, event.getKey().toString(), event.getValue());
                }
            } catch (Exception e) {
                logger.severe("Error emitting events: " + e.getMessage());
            }
        }, 0, emitEveryNthSecond, TimeUnit.SECONDS);
    }

    public void close() {
        executor.shutdown();
        messageBroker.closeProducer(producer);
    }
}