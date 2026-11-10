package com.healthyrolls.technicalservices.configuration;

import java.util.List;
import java.util.UUID;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

public record SeedRestaurant(
        UUID id,
        String name,
        RestaurantStatus status,
        double latitude,
        double longitude,
        List<SeedMenuItem> menu) {}