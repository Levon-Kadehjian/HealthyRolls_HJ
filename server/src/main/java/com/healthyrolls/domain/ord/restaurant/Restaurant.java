package com.healthyrolls.domain.ord.restaurant;

import java.util.Objects;
import java.util.UUID;

public class Restaurant {

    private final UUID restaurantId;
    private final String name;
    private RestaurantStatus status;
    private final double latitude;
    private final double longitude;

    public Restaurant(UUID restaurantId, String name, RestaurantStatus status, double latitude, double longitude) {

        this.restaurantId = Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        this.name = Objects.requireNonNull(name, "Restaurant name is required");
        this.status = Objects.requireNonNull(status, "Restaurant status is required");

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        this.latitude = latitude;
        this.longitude = longitude;

        if (name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name cannot be blank");
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isOpen() {
        return status == RestaurantStatus.OPEN;
    }

    public void pause() {
        status = RestaurantStatus.PAUSED;
    }

    public void reopen() {
        if (status != RestaurantStatus.PAUSED) {
            throw new IllegalStateException("Only paused restaurants can be reopened");
        }

        status = RestaurantStatus.OPEN;
    }
}