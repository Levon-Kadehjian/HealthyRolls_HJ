package com.healthyrolls.domain.ord.order;

import java.util.List;
import java.util.UUID;

public class OrderFactory {

    private final OrderStateFactory stateFactory = new OrderStateFactory();

    public Order createNew(UUID customerId, UUID restaurantId, List<OrderLine> items) {

        return new Order(customerId, restaurantId, items);
    }

    public Order reconstitute(OrderRecord record) {
        return new Order(record, stateFactory);
    }
}
