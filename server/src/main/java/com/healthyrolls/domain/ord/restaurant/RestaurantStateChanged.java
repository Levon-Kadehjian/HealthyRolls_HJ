package com.healthyrolls.domain.ord.restaurant;

import java.time.Instant;
import java.util.UUID;

public record RestaurantStateChanged(
        UUID eventId,
        UUID restaurantId,
        RestaurantStatus fromStatus,
        RestaurantStatus toStatus,
        UUID userId,
        Instant occurredAt) {}
