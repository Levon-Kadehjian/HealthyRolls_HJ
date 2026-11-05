package com.healthyrolls.domain.dlv.exceptions;

import com.healthyrolls.domain.ord.order.OrderId;

public class OfferNotAcceptedException extends RuntimeException {

    public OfferNotAcceptedException(OrderId orderId) {
        super("Offer for order " + orderId.value() + " has not been accepted");
    }
}
