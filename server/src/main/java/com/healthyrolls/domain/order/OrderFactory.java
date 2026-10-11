package com.healthyrolls.domain.order;

import java.util.List;

import com.healthyrolls.domain.order.common.CustomerId;
import com.healthyrolls.domain.order.common.RestaurantId;

public class OrderFactory {

    private OrderStateFactory stateFactory;

    public Order createNew(
            CustomerId customerId,
            RestaurantId restaurantId,
            List<OrderItem> items) {
        throw new UnsupportedOperationException();
    }

    public Order fromRecord(OrderRecord record) {
        throw new UnsupportedOperationException();
    }
}