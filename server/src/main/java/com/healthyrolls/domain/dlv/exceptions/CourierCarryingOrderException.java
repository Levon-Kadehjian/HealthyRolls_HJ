package com.healthyrolls.domain.dlv.exceptions;

import com.healthyrolls.domain.common.UserId;

public class CourierCarryingOrderException extends RuntimeException {

    public CourierCarryingOrderException(UserId courierId) {
        super("Courier " + courierId.value() + " cannot go off duty while carrying an order");
    }
}
