package com.healthyrolls.domain.ord.restaurant;

import java.util.Objects;
import java.util.UUID;

public class Restaurant {

    private final UUID restaurantId;
    private final String name;
    private RestaurantStatus status;

    public Restaurant(
            UUID restaurantId,
            String name,
            RestaurantStatus status) {

        this.restaurantId = Objects.requireNonNull(
                restaurantId, "Restaurant ID is required");
        this.name = Objects.requireNonNull(
                name, "Restaurant name is required");
        this.status = Objects.requireNonNull(
                status, "Restaurant status is required");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Restaurant name cannot be blank");
        }
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public boolean isOpen() {
        return status == RestaurantStatus.OPEN;
    }
}