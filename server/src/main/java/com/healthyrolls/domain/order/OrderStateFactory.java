package com.healthyrolls.domain.order;

public class OrderStateFactory {

    public AbstractOrderState fromStatus(
            OrderStatus status,
            Order order) {
        throw new UnsupportedOperationException();
    }
}