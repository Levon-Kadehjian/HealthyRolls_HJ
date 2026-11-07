package com.healthyrolls.domain.ord.restaurant;

import java.util.UUID;
import java.util.List;

public interface IRestaurantRepository {

    List<Restaurant> findAll();

    Restaurant findById(UUID restaurantId);

    void lockById(UUID restaurantId);

    Restaurant save(Restaurant restaurant);

}