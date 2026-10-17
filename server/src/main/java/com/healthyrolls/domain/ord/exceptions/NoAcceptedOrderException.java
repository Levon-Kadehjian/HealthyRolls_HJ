package com.healthyrolls.domain.ord.exceptions;

import java.util.UUID;

public class NoAcceptedOrderException extends RuntimeException {

    public NoAcceptedOrderException(UUID restaurantId) {
        super("No accepted order waiting for restaurant: " + restaurantId);
    }
}