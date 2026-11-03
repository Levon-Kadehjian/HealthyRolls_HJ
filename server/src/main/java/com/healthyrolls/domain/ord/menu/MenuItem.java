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

    public void edit(String name, BigDecimal price) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Name must contain between 1 and 255 characters");
        }
        if (price == null || price.signum() < 0 || price.stripTrailingZeros().scale() > 2
                || price.compareTo(new BigDecimal("100000000000000000")) >= 0) {
            throw new IllegalArgumentException("Price must be non-negative and fit a monetary amount with two decimal places");
        }
        this.name = name.strip();
        this.price = price;
    }

    public void changeAvailability(MenuItemStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Availability is required");
        }
        this.status = status;
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
