package com.healthyrolls.domain.ord.exceptions;

import java.util.UUID;

public class RestaurantUnavailableException extends RuntimeException {

    public RestaurantUnavailableException(UUID restaurantId) {
        super("Restaurant is not open: " + restaurantId);
    }
}