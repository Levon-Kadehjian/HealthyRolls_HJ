package com.healthyrolls.domain.order;

import java.time.Instant;

import com.healthyrolls.domain.order.common.CourierId;
import com.healthyrolls.domain.order.common.CustomerId;
import com.healthyrolls.domain.order.common.RestaurantId;

public class Order {

    private OrderId id;
    private CustomerId customerId;
    private RestaurantId restaurantId;
    private CourierId courierId;
    private AbstractOrderState state;
    private Instant acceptedAt;
    private long version;

    public void assignCourier(CourierId id) {
        throw new UnsupportedOperationException();
    }

    public void apply(OrderAction action) {
        throw new UnsupportedOperationException();
    }

    public OrderStatus status() {
        throw new UnsupportedOperationException();
    }

    void changeState(AbstractOrderState next) {
        throw new UnsupportedOperationException();
    }
}