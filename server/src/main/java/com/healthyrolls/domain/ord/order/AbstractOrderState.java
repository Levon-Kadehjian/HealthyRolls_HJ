package com.healthyrolls.domain.ord.order;

import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;

public abstract class AbstractOrderState {

    protected Order order;

    public abstract OrderStatus status();

    public void accept() {
        throw new InvalidTransitionException("Cannot accept an order in state " + status());
    }

    public void startPreparing() {
        throw new InvalidTransitionException("Cannot start preparing an order in state " + status());
    }

    public void markReady() {
        throw new InvalidTransitionException("Cannot mark an order ready in state " + status());
    }

    public void markPickedUp() {
        throw new UnsupportedOperationException();
    }

    public void markDelivered() {
        throw new UnsupportedOperationException();
    }

    public void cancel() {
        throw new InvalidTransitionException(
                "Cannot cancel an order in state " + status());
    }

    public void reject() {
        throw new InvalidTransitionException(
                "Cannot reject an order in state " + status());
    }

    protected void transitionTo(AbstractOrderState next) {
        order.changeState(next);
    }

}