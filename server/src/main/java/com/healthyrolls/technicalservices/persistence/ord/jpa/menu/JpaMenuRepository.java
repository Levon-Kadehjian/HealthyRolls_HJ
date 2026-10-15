package com.healthyrolls.technicalservices.persistence.ord.jpa.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.menu.Menu;
import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.domain.ord.menu.MenuRepository;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.SpringDataRestaurantRepository;

@Repository
public class JpaMenuRepository implements MenuRepository {

    private final SpringDataMenuItemRepository database;
    private final SpringDataRestaurantRepository restaurants;

    public JpaMenuRepository(
            SpringDataMenuItemRepository database,
            SpringDataRestaurantRepository restaurants) {

        this.database = database;
        this.restaurants = restaurants;
    }

    @Override
    @Transactional(readOnly = true)
    public Menu findByRestaurantId(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        if (!restaurants.existsById(restaurantId)) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        List<MenuItemEntity> entities = database.findByRestaurant_RestaurantId(restaurantId);

        List<MenuItem> items = new ArrayList<>();

        for (MenuItemEntity entity : entities) {
            items.add(new MenuItem(
                    entity.getItemId(),
                    entity.getName(),
                    entity.getPrice(),
                    entity.getStatus()));
        }

        return new Menu(restaurantId, items);
    }
}