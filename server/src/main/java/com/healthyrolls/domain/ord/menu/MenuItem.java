package com.healthyrolls.domain.ord.menu;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class MenuItem {

    private final UUID itemId;
    private String name;
    private BigDecimal price;
    private MenuItemStatus status;

    public MenuItem(
            UUID itemId,
            String name,
            BigDecimal price,
            MenuItemStatus status) {

        this.itemId = Objects.requireNonNull(itemId, "Item ID is required");
        this.name = Objects.requireNonNull(name, "Name is required");
        this.price = Objects.requireNonNull(price, "Price is required");
        this.status = Objects.requireNonNull(status, "Status is required");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    public UUID getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public MenuItemStatus getStatus() {
        return status;
    }
}