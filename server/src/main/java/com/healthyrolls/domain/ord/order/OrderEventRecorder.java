package com.healthyrolls.domain.ord.order;

public interface OrderEventRecorder {

    void record(OrderStateChanged event);
}
