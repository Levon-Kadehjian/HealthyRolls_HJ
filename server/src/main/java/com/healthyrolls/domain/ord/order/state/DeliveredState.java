package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public final class DeliveredState extends AbstractOrderState {

    public static final DeliveredState INSTANCE = new DeliveredState();

    private DeliveredState() {
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.DELIVERED;
    }
}
