package org.emitters;

import org.messagebroker.KafkaBroker;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.streams.ProductEventCounter;
import org.apache.spark.sql.streaming.StreamingQueryException;
import java.util.concurrent.TimeoutException;
import org.apache.spark.sql.streaming.StreamingQuery;

public class FastStreamingMain {

    private static final Logger logger = Logger.getLogger(FastStreamingMain.class.getName());

    public static void main(String[] args) {
        var eventCreator = new RetailRandomEventCreator(5);
        var kafkaHost = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        var messageBroker = new KafkaBroker(kafkaHost.split(":")[0], 
                                               Integer.parseInt(kafkaHost.split(":")[1]));
        try {
            if (!messageBroker.doesTopicExist("product-events")) {
                messageBroker.createTopic("product-events", 2, (short) 1);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Error creating topic: " + e.getMessage());
            System.exit(1);
        }
        var emitter = new MessageBrokerEventEmitter(eventCreator, messageBroker, 5);

        
        Thread emitterThread = new Thread(() -> {
            emitter.emitEvents();
        });
        emitterThread.start();

        var productEventCounter = new ProductEventCounter();
        StreamingQuery query = null;
        try {
            query = productEventCounter.countAndPersistEventsInDatabase();
        } catch (TimeoutException | StreamingQueryException e) {
            logger.severe("Error counting and persisting events: " + e.getMessage());
            System.exit(1);
        }

        final var streamQuery = query;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down emitter");
            emitter.close();
            if (streamQuery != null) {
                try {
                    streamQuery.stop();
                } catch (TimeoutException e) {
                    logger.severe("Error stopping query: " + e.getMessage());
                }
            }
        }));
    }
}
