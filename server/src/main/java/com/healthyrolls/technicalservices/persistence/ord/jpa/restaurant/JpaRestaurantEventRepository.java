package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import com.healthyrolls.domain.ord.restaurant.IRestaurantEventRepository;
import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;

import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaRestaurantEventRepository implements IRestaurantEventRepository {

    private final SpringDataRestaurantEventRepository database;

    public JpaRestaurantEventRepository(SpringDataRestaurantEventRepository database) {
        this.database = database;
    }

    @Override
    @Transactional
    public void save(RestaurantStateChanged event) {
        Objects.requireNonNull(event, "Restaurant event is required");
        database.save(new RestaurantEventEntity(event));
    }
}
