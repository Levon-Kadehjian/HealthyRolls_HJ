package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class DeliveredState extends AbstractOrderState {

    public static final DeliveredState INSTANCE = new DeliveredState();
    public static final AbstractOrderState REFUNDED = new AbstractOrderState() {
        @Override
        public OrderStatus status() {
            return OrderStatus.REFUNDED;
        }
    };

    private DeliveredState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.DELIVERED;
    }

    @Override
    public void refund(com.healthyrolls.domain.ord.order.Order order) {
        transitionTo(order, REFUNDED);
    }
}
