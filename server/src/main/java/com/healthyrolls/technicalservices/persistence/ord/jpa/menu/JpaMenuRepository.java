package com.healthyrolls.technicalservices.persistence.ord.jpa.menu;

import com.healthyrolls.domain.ord.menu.IMenuRepository;
import com.healthyrolls.domain.ord.exceptions.RestaurantNotFoundException;
import com.healthyrolls.domain.ord.menu.Menu;
import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.SpringDataRestaurantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaMenuRepository implements IMenuRepository {

    private final SpringDataMenuItemRepository database;
    private final SpringDataRestaurantRepository restaurants;

    public JpaMenuRepository(
            SpringDataMenuItemRepository database,
            SpringDataRestaurantRepository restaurants) {

        this.database = database;
        this.restaurants = restaurants;
    }

    @Override
    @Transactional
    public MenuItem saveItem(UUID restaurantId, MenuItem item) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        Objects.requireNonNull(item, "Menu item is required");
        var restaurant = restaurants.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        MenuItemEntity saved = database.save(new MenuItemEntity(item.getItemId(), restaurant,
                item.getName(), item.getPrice(), item.getStatus()));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Menu findByRestaurantId(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        if (!restaurants.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }

        List<MenuItemEntity> entities = database.findByRestaurantRestaurantId(restaurantId);

        List<MenuItem> items = new ArrayList<>();

        for (MenuItemEntity entity : entities) {
            items.add(toDomain(entity));
        }

        return new Menu(restaurantId, items);
    }

    private MenuItem toDomain(MenuItemEntity entity) {
        return new MenuItem(entity.getItemId(), entity.getName(), entity.getPrice(), entity.getStatus());
    }
}
