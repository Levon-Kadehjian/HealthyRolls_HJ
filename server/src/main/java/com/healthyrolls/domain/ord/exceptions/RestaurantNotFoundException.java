package com.healthyrolls.domain.ord.exceptions;

import java.util.UUID;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(UUID restaurantId) {
        super("Restaurant not found: " + restaurantId);
    }
}
