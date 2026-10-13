package com.healthyrolls.domain.ord.order;

import java.util.List;
import java.util.UUID;

public class OrderFactory {

    private OrderStateFactory stateFactory;

    public Order createNew(
            UUID customerId,
            UUID restaurantId,
            List<OrderLine> items) {

        return new Order(customerId, restaurantId, items);
    }

    public Order fromRecord(OrderRecord record) {
        throw new UnsupportedOperationException();
    }
}
