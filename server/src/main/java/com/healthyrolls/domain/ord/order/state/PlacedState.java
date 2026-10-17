package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

import java.util.Objects;

public class PlacedState extends AbstractOrderState {

    public PlacedState(Order order) {
        this.order = Objects.requireNonNull(order);
    }

    @Override
    public void accept() {
        transitionTo(new AcceptedState());
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PLACED;
    }
}
