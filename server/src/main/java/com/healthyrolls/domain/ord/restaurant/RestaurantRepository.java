package com.healthyrolls.domain.ord.restaurant;

import java.util.UUID;

public interface RestaurantRepository {

    Restaurant findById(UUID restaurantId);

    Restaurant save(Restaurant restaurant);

}