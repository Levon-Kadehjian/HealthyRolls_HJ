package com.healthyrolls.technicalservices.persistence.ord.jpa.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.menu.IMenuEventRepository;
import com.healthyrolls.domain.ord.menu.MenuItemStateChanged;

@Repository
public class JpaMenuEventRepository implements IMenuEventRepository {

    private final SpringDataMenuEventRepository database;

    public JpaMenuEventRepository(SpringDataMenuEventRepository database) {
        this.database = database;
    }

    @Override
    @Transactional
    public void save(MenuItemStateChanged event) {
        Objects.requireNonNull(event, "Menu event is required");
        database.save(new MenuEventEntity(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemStateChanged> findByRestaurantId(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        List<MenuItemStateChanged> events = new ArrayList<>();

        for (MenuEventEntity entity : database.findByRestaurantIdOrderByOccurredAt(restaurantId)) {
            events.add(new MenuItemStateChanged(
                    entity.getEventId(),
                    entity.getItemId(),
                    entity.getRestaurantId(),
                    entity.getFromStatus(),
                    entity.getToStatus(),
                    entity.getUserId(),
                    entity.getOccurredAt()));
        }

        return List.copyOf(events);
    }
}
