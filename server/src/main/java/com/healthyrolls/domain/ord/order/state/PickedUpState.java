package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class PickedUpState extends AbstractOrderState {

    public static final PickedUpState INSTANCE = new PickedUpState();

    private PickedUpState() {
    }

    @Override
    public void markDelivered(Order order) {
        transitionTo(order, DeliveredState.INSTANCE);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PICKED_UP;
    }
}
