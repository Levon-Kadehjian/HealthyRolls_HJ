package com.healthyrolls.technicalservices.persistence;

import java.util.UUID;

import com.healthyrolls.domain.ord.restaurant.Restaurant;

public interface RestaurantRepository {

    Restaurant findById(UUID restaurantId);

}