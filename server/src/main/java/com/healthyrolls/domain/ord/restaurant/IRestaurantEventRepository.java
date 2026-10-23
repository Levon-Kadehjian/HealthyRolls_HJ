package com.healthyrolls.domain.ord.restaurant;

import java.util.List;
import java.util.UUID;

public interface IRestaurantEventRepository {

    void save(RestaurantStateChanged event);

    List<RestaurantStateChanged> findByRestaurantId(UUID restaurantId);
}
