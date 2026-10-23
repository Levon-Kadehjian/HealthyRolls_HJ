package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.restaurant.IRestaurantEventRepository;
import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;

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

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantStateChanged> findByRestaurantId(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        List<RestaurantStateChanged> events = new java.util.ArrayList<>();

        for (RestaurantEventEntity entity : database.findByRestaurantIdOrderByOccurredAt(restaurantId)) {
            events.add(new RestaurantStateChanged(
                    entity.getEventId(),
                    entity.getRestaurantId(),
                    entity.getFromStatus(),
                    entity.getToStatus(),
                    entity.getUserId(),
                    entity.getOccurredAt()));
        }

        return List.copyOf(events);
    }
}
