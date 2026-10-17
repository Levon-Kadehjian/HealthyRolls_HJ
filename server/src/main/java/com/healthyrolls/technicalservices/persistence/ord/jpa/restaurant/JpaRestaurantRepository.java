package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

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

        return new Restaurant(entity.getRestaurantId(), entity.getName(), entity.getStatus(),
                entity.getLatitude(), entity.getLongitude());
    }

    @Override
    public Restaurant save(Restaurant restaurant) {

        RestaurantEntity entity = new RestaurantEntity(restaurant.getRestaurantId(), restaurant.getName(),
                restaurant.getStatus(), restaurant.getLatitude(), restaurant.getLongitude());
        RestaurantEntity saved = database.save(entity);
        return new Restaurant(saved.getRestaurantId(), saved.getName(), saved.getStatus(),
                saved.getLatitude(), saved.getLongitude());
    }
}
