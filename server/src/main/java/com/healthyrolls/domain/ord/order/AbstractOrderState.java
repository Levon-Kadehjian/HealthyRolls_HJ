package com.healthyrolls.domain.ord.order;

import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;

public abstract class AbstractOrderState {

    public abstract OrderStatus status();

    public void accept(Order order) {
        throw new InvalidTransitionException("Cannot accept an order in state " + status());
    }

    public void startPreparing(Order order) {
        throw new InvalidTransitionException("Cannot start preparing an order in state " + status());
    }

    public void markReady(Order order) {
        throw new InvalidTransitionException("Cannot mark an order ready in state " + status());
    }

    public void markPickedUp(Order order) {
        throw new UnsupportedOperationException();
    }

    public void markDelivered(Order order) {
        throw new UnsupportedOperationException();
    }

    public void cancel(Order order) {
        throw new InvalidTransitionException(
                "Cannot cancel an order in state " + status());
    }

    public void reject(Order order) {
        throw new InvalidTransitionException(
                "Cannot reject an order in state " + status());
    }

    protected void transitionTo(Order order, AbstractOrderState next) {
        order.changeState(next);
    }

}
