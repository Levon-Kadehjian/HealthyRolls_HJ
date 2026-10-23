package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class PreparingState extends AbstractOrderState {

    public static final PreparingState INSTANCE = new PreparingState();

    private PreparingState() {
    }

    @Override
    public void markReady(Order order) {
        transitionTo(order, ReadyState.INSTANCE);
    }

    @Override
    public void cancel(Order order) {
        transitionTo(order, CancelledState.INSTANCE);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PREPARING;
    }
}
