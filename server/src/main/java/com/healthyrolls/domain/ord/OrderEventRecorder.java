package com.healthyrolls.domain.ord;

public interface OrderEventRecorder {

    void record(OrderStateChanged event);
}