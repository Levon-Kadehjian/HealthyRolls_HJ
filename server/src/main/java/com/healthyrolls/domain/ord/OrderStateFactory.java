package com.healthyrolls.domain.ord;

public class OrderStateFactory {

    public AbstractOrderState fromStatus(
            OrderStatus status,
            Order order) {
        throw new UnsupportedOperationException();
    }
}