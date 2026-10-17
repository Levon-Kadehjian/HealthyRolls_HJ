package com.healthyrolls.domain.ord.exceptions;

import com.healthyrolls.domain.ord.order.OrderId;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(OrderId orderId) {
        super("Order not found: " + orderId.value());
    }
}