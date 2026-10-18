package com.healthyrolls.domain.ord.restaurant;

import java.util.UUID;

public interface IRestaurantRepository {

    Restaurant findById(UUID restaurantId);

    Restaurant save(Restaurant restaurant);

}