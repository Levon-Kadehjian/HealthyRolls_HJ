package com.healthyrolls.domain.ord.order;

public abstract class AbstractOrderState {

    protected Order order;

    public abstract OrderStatus status();

    public void accept() {
        throw new UnsupportedOperationException();
    }

    public void markReady() {
        throw new UnsupportedOperationException();
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

    public void startPreparing() {
        throw new UnsupportedOperationException();
    }

}