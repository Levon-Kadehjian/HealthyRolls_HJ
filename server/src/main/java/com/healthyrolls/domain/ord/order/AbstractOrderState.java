package com.healthyrolls.domain.ord.order;

public abstract class AbstractOrderState {

    protected Order order;

    public abstract OrderStatus status();

    public void accept() {
        throw new IllegalStateException("Cannot accept an order in state " + status());
    }

    public void startPreparing() {
        throw new IllegalStateException("Cannot start preparing an order in state " + status());
    }

    public void markReady() {
        throw new IllegalStateException("Cannot mark an order ready in state " + status());
    }

    public void markPickedUp() {
        throw new UnsupportedOperationException();
    }

    public void markDelivered() {
        throw new UnsupportedOperationException();
    }

    public void cancel() {
        throw new UnsupportedOperationException();
    }

    protected void transitionTo(AbstractOrderState next) {
        order.changeState(next);
    }

}