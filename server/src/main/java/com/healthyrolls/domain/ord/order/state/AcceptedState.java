package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class AcceptedState extends AbstractOrderState {

    public static final AcceptedState INSTANCE = new AcceptedState();

    private AcceptedState() {
    }

    @Override
    public void startPreparing(Order order) {
        transitionTo(order, PreparingState.INSTANCE);
    }

    @Override
    public void cancel(Order order) {
        transitionTo(order, CancelledState.INSTANCE);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.ACCEPTED;
    }
}
