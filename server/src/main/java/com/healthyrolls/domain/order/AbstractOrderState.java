package com.healthyrolls.domain.order;

public abstract class AbstractOrderState {

    protected Order order;

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

    public OrderStatus status() {
        throw new UnsupportedOperationException();
    }
}