package com.healthyrolls.domain.ord.order.state;

import java.util.Objects;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public class PlacedState extends AbstractOrderState {

    public PlacedState(Order order) {
        this.order = Objects.requireNonNull(order);
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PLACED;
    }
}