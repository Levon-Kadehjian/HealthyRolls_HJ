package com.healthyrolls.domain.ord.order.state;

import com.healthyrolls.domain.ord.order.AbstractOrderState;
import com.healthyrolls.domain.ord.order.OrderStatus;

public class AcceptedState extends AbstractOrderState {

    @Override
    public void startPreparing() {
        transitionTo(new PreparingState());
    }

    @Override
    public OrderStatus status() {
        return OrderStatus.ACCEPTED;
    }
}