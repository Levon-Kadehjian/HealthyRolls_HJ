package com.healthyrolls.domain.ord.exceptions;

public class InvalidRestaurantTransitionException extends IllegalStateException {
    public InvalidRestaurantTransitionException(String message) {
        super(message);
    }
}
