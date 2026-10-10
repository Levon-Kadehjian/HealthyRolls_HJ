package com.healthyrolls.domain.order;

public interface OrderEventRecorder {

    void record(OrderStateChanged event);
}