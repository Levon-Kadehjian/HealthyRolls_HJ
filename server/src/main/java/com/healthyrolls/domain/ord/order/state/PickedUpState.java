package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class PickedUpState extends AbstractOrderState {

    public static final PickedUpState INSTANCE = new PickedUpState();

    private PickedUpState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PICKED_UP;
    }
}
