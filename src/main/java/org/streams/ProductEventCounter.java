package org.streams;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.streaming.StreamingQuery;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import static org.apache.spark.sql.functions.*;
import org.apache.spark.sql.streaming.StreamingQueryException;
import java.util.logging.Logger;

public class ProductEventCounter {

    private static final Logger logger = Logger.getLogger(ProductEventCounter.class.getName());

    public StreamingQuery countAndPersistEventsInDatabase() throws TimeoutException, StreamingQueryException {
        // Create SparkSession
        SparkSession spark = SparkSession.builder()
        .appName("EventCount")
        .master("local[*]")
        .config("spark.driver.host", "127.0.0.1")
        .config("spark.driver.bindAddress", "127.0.0.1")
        .config("spark.executor.memory", "512m")
        .config("spark.driver.memory", "512m")
        .getOrCreate();

        // Read streaming data from Kafka
        Dataset<Row> kafkaDF = spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"))
                .option("subscribe", "product-events")
                .load()
                .select(
                    col("key").cast("string").alias("eventType"),
                    col("value").cast("string").alias("product")
                );

        Dataset<Row> aggregatedDF = kafkaDF
        .groupBy(col("product"), col("eventType"))
        .agg(count("*").alias("eventCount"));

        // Database connection properties
        String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
        Properties connectionProperties = new Properties();
        connectionProperties.put("user", "postgres");
        connectionProperties.put("password", "postgres");
        // Uncomment and adjust the driver if necessary:
        // connectionProperties.put("driver", "org.postgresql.Driver");

        // Write the results to the database using foreachBatch
        StreamingQuery query = aggregatedDF.writeStream()
                .outputMode("update") // or "complete" if you want full aggregation state
                .foreachBatch((batchDF, batchId) -> {
                    // Write each micro-batch to the database table "event_counts"
                    logger.info("Writing batch " + batchId + " to database");
                    batchDF.write()
                           .mode("append")
                           .jdbc(jdbcUrl, "event_counts", connectionProperties);
                })
                .start();

        // Await termination
        query.awaitTermination();

        return query;
    }
}
