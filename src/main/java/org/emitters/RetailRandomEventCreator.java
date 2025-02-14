package org.emitters;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

// Move enum outside the class
enum EventType {
    PRODUCT_VIEWED,
    PRODUCT_ADDED_TO_CART,
    PRODUCT_PURCHASED
}

/**
 * This class emits random ecommerce events for a retail company.
 * The events generated are:
 * - Product Viewed
 * - Product Added to Cart
 * - Product Purchased
 * 
 * The key of the map is the event type and the value is the product id.
 * 
 * Each invocation of emitEvents will return all 3 events with random product ids from 0 to numberOfProducts - 1.
 */
public class RetailRandomEventCreator implements RandomEventCreator<EventType> {

    private final List<String> productIds;

    public RetailRandomEventCreator(int numberOfProducts) {
        this.productIds = IntStream.range(0, numberOfProducts)
            .mapToObj(String::valueOf)
            .collect(Collectors.toList());
    }
    
    @Override
    public Map<EventType, String> emitEvents() {
        var events = new HashMap<EventType, String>();
        for (int i = 0; i < EventType.values().length; i++) {
            var eventType = EventType.values()[i];
            var productId = productIds.get(ThreadLocalRandom.current().nextInt(0, productIds.size()));
            events.put(eventType, productId);
        }
        return events;
    }
}
