package com.healthyrolls.domain.dlv.exceptions;

import com.healthyrolls.domain.ord.order.OrderId;

public class OfferNotFoundException extends RuntimeException {

    public OfferNotFoundException(OrderId orderId) {
        super("No pending offer found for order " + orderId.value());
    }
}
