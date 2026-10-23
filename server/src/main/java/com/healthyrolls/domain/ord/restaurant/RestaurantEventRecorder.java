package com.healthyrolls.domain.ord.restaurant;

public interface RestaurantEventRecorder {

    void record(RestaurantStateChanged event);
}
