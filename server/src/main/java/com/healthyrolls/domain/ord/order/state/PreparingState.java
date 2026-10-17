package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public class PreparingState extends AbstractOrderState {

    @Override
    public void markReady() {
        transitionTo(new ReadyState());
    }

    @Override
    public void cancel() {
        transitionTo(new CancelledState());
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.PREPARING;
    }
}