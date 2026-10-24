package com.healthyrolls.application.ord.controllers;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

public record RestaurantEventResponse(
        UUID eventId,
        UUID restaurantId,
        RestaurantStatus fromStatus,
        RestaurantStatus toStatus,
        UUID userId,
        Instant occurredAt) {

    public static RestaurantEventResponse from(RestaurantStateChanged e) {
        return new RestaurantEventResponse(
                e.eventId(),
                e.restaurantId(),
                e.fromStatus(),
                e.toStatus(),
                e.userId(),
                e.occurredAt());
    }
}
