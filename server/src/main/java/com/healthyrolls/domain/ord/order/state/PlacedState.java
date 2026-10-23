package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class PlacedState extends AbstractOrderState {

    public static final PlacedState INSTANCE = new PlacedState();

    private PlacedState() {
    }

    @Override
    public void accept(Order order) {
        transitionTo(order, AcceptedState.INSTANCE);
    }

    @Override
    public void reject(Order order) {
        transitionTo(order, RejectedState.INSTANCE);
    }

    @Override
    public void cancel(Order order) {
        transitionTo(order, CancelledState.INSTANCE);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PLACED;
    }
}
