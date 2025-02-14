package org.emitters;

import org.messagebroker.KafkaBroker;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class EmitterMain {

    private static final Logger logger = Logger.getLogger(EmitterMain.class.getName());

    public static void main(String[] args) {
        var eventCreator = new RetailRandomEventCreator(10);
        var messageBroker = new KafkaBroker("localhost", 9092);
        try {
            messageBroker.createTopic("product-events", 2, (short) 1);
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Error creating topic: " + e.getMessage());
            System.exit(1);
        }
        var emitter = new MessageBrokerEventEmitter(eventCreator, messageBroker, 5);
        emitter.emitEvents();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down emitter");
            emitter.close();
        }));
    }
}
