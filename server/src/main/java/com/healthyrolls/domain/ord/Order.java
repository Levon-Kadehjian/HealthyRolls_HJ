package com.healthyrolls.domain.ord;

import java.time.Instant;
import java.util.UUID;

public class Order {

    private OrderId id;
    private UUID customerId;
    private UUID restaurantId;
    private UUID courierId;
    private AbstractOrderState state;
    private Instant acceptedAt;
    private long version;

    public void assignCourier(UUID id) {
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