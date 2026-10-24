package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class RejectedState extends AbstractOrderState {

    public static final RejectedState INSTANCE = new RejectedState();

    private RejectedState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.REJECTED;
    }
}
