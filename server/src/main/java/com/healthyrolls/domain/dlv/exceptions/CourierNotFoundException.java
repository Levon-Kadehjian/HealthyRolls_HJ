package com.healthyrolls.domain.dlv.exceptions;

import com.healthyrolls.domain.common.UserId;

public class CourierNotFoundException extends RuntimeException {

    public CourierNotFoundException(UserId courierId) {
        super("Courier not found: " + courierId.value());
    }
}
