package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

public class AcceptedState extends AbstractOrderState {

    @Override
    public void startPreparing(Order order) {
        transitionTo(order, new PreparingState());
    }

    @Override
    public void cancel(Order order) {
        transitionTo(order, new CancelledState());
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.ACCEPTED;
    }
}
