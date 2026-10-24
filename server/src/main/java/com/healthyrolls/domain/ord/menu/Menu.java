package com.healthyrolls.domain.ord.menu;

import com.healthyrolls.domain.ord.exceptions.MenuItemSoldOutException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Menu {

    private final UUID restaurantId;
    private final List<MenuItem> items;

    public Menu(UUID restaurantId, List<MenuItem> items) {
        this.restaurantId = Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        this.items = List.copyOf(Objects.requireNonNull(items, "Menu items are required"));
        Set<UUID> ids = new HashSet<>();
        for (MenuItem item : this.items) {
            if (!ids.add(item.getItemId())) {
                throw new IllegalArgumentException("Duplicate menu item ID");
            }
        }
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public MenuItem findItem(UUID itemId) {
        Objects.requireNonNull(itemId, "Menu item ID is required");
        for (MenuItem item : items) {
            if (item.getItemId().equals(itemId)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Item does not belong to this restaurant's menu");
    }

    public MenuItem findAvailableItem(UUID itemId) {
        MenuItem item = findItem(itemId);

        if (item.getStatus() != MenuItemStatus.AVAILABLE) {
            throw new MenuItemSoldOutException(itemId);
        }

        return item;
    }
}
