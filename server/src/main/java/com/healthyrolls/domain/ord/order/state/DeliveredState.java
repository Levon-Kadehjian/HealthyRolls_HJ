package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public class DeliveredState extends AbstractOrderState {

    @Override
    public OrderStatus status() {
        return OrderStatus.DELIVERED;
    }
}
