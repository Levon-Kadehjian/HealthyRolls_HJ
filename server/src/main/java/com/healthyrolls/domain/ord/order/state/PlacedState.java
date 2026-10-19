package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public class PlacedState extends AbstractOrderState {

    @Override
    public void accept(Order order) {
        transitionTo(order, new AcceptedState());
    }

    @Override
    public void reject(Order order) {
        transitionTo(order, new RejectedState());
    }

    @Override
    public void cancel(Order order) {
        transitionTo(order, new CancelledState());
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PLACED;
    }
}
