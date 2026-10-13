package com.healthyrolls.application.ord;

import java.util.Objects;
import java.util.UUID;

import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.technicalservices.persistence.RestaurantRepository;

public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = Objects.requireNonNull(repository,  "Restaurant repository is required");
    }

    public boolean isOpen(UUID restaurantId) {
        Objects.requireNonNull(
                restaurantId, "Restaurant ID is required");

        Restaurant restaurant = repository.findById(restaurantId);

        return restaurant.isOpen();
    }
}