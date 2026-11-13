package com.healthyrolls.application.dash.controllers;

import java.util.UUID;

import com.healthyrolls.domain.ord.order.QueueCounts;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

public record MapRestaurantResponse(UUID id, String name, RestaurantStatus status, double latitude, double longitude,
        long queuedCount, long preparingCount, long readyCount) {

    public static MapRestaurantResponse from(Restaurant restaurant, QueueCounts counts) {
        return new MapRestaurantResponse(restaurant.getRestaurantId(), restaurant.getName(), restaurant.getStatus(),
                restaurant.getLatitude(), restaurant.getLongitude(),
                counts == null ? 0 : counts.queued(), counts == null ? 0 : counts.preparing(),
                counts == null ? 0 : counts.ready());
    }
}
