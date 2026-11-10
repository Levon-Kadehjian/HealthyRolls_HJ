package com.healthyrolls.domain.ord.order;

import java.util.List;
import java.util.UUID;

public class OrderFactory {

    private final OrderStateFactory stateFactory = new OrderStateFactory();

    public Order createNew(UUID customerId, UUID restaurantId, List<OrderLine> items,
            Double deliveryLatitude, Double deliveryLongitude) {

        return new Order(customerId, restaurantId, items, deliveryLatitude, deliveryLongitude);
    }

    public Order reconstitute(OrderRecord record) {
        return new Order(record, stateFactory);
    }
}
