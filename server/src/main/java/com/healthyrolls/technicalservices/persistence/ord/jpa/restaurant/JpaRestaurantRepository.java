package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import com.healthyrolls.domain.ord.restaurant.IRestaurantRepository;
import com.healthyrolls.domain.ord.exceptions.RestaurantNotFoundException;
import com.healthyrolls.domain.ord.restaurant.Restaurant;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRestaurantRepository implements IRestaurantRepository {

    private final SpringDataRestaurantRepository database;

    public JpaRestaurantRepository(
            SpringDataRestaurantRepository database) {
        this.database = database;
    }

    @Override
    public List<Restaurant> findAll() {
        List<Restaurant> result = new ArrayList<>();
        for (RestaurantEntity entity : database.findAll()) {
            result.add(new Restaurant(entity.getRestaurantId(), entity.getName(), entity.getStatus(),
                    entity.getLatitude(), entity.getLongitude()));
        }
        return result;
    }

    @Override
    public Restaurant findById(UUID restaurantId) {

        RestaurantEntity entity = database.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

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
