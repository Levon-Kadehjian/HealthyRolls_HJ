package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;
import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class ReadyState extends AbstractOrderState {

    public static final ReadyState INSTANCE = new ReadyState();

    private ReadyState() {
    }

    @Override
    public void markPickedUp(Order order) {
        if (order.getCourierId() == null) {
            throw new InvalidTransitionException("Cannot pick up an order with no assigned courier");
        }
        transitionTo(order, PickedUpState.INSTANCE);
    }

    @Override
    public void cancel(Order order) {
        transitionTo(order, CancelledState.INSTANCE);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.READY;
    }
}
