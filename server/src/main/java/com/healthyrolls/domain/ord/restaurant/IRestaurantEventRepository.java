package com.healthyrolls.domain.ord.restaurant;

public interface IRestaurantEventRepository {
    void save(RestaurantStateChanged event);
}
