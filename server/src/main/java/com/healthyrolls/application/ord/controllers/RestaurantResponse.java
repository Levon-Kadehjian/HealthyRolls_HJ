package com.healthyrolls.application.ord.controllers;

import java.util.UUID;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

public record RestaurantResponse(UUID id, String name, RestaurantStatus status,
        double latitude, double longitude) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getRestaurantId(), restaurant.getName(),
                restaurant.getStatus(), restaurant.getLatitude(), restaurant.getLongitude());
    }
}
