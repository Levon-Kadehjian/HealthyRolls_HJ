package com.healthyrolls.technicalservices.persistence.ord;

import java.util.UUID;

import com.healthyrolls.domain.ord.restaurant.Restaurant;

public interface RestaurantRepository {

    Restaurant findById(UUID restaurantId);

    Restaurant save(Restaurant restaurant);

}