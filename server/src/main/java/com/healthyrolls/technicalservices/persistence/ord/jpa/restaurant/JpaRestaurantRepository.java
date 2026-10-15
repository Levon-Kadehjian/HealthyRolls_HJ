package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantRepository;

@Repository
public class JpaRestaurantRepository implements RestaurantRepository {

    private final SpringDataRestaurantRepository database;

    public JpaRestaurantRepository(
            SpringDataRestaurantRepository database) {
        this.database = database;
    }

    @Override
    public Restaurant findById(UUID restaurantId) {

        RestaurantEntity entity = database.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        return new Restaurant(entity.getRestaurantId(), entity.getName(), entity.getStatus());
    }

    @Override
    public Restaurant save(Restaurant restaurant) {

        RestaurantEntity entity = new RestaurantEntity(restaurant.getRestaurantId(), restaurant.getName(),
                restaurant.getStatus());
        RestaurantEntity saved = database.save(entity);
        return new Restaurant(saved.getRestaurantId(), saved.getName(), saved.getStatus());
    }
}