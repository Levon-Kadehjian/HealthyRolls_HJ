package com.healthyrolls.domain.ord.restaurant;

public interface RestaurantEventRepository {
    void save(RestaurantStateChanged event);
}
