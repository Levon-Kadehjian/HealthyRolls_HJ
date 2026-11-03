package com.healthyrolls.domain.dlv.exceptions;

import com.healthyrolls.domain.ord.order.OrderId;

public class OrderNotStalledException extends RuntimeException {

    public OrderNotStalledException(OrderId orderId) {
        super("Order " + orderId.value() + " is not stalled and cannot be reassigned");
    }
}
