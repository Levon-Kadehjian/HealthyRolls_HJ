package com.healthyrolls.domain.ord;

import java.util.List;
import java.util.UUID;

public class OrderFactory {

    private OrderStateFactory stateFactory;

    public Order createNew(
            UUID customerId,
            UUID restaurantId,
            List<OrderItem> items) {
        throw new UnsupportedOperationException();
    }

    public Order fromRecord(OrderRecord record) {
        throw new UnsupportedOperationException();
    }
}