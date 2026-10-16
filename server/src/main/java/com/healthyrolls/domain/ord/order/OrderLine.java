package com.healthyrolls.domain.ord.order;

import com.healthyrolls.domain.ord.menu.MenuItem;

import java.util.Objects;

public record OrderLine(MenuItem menuItem, int quantity) {

    public OrderLine {
        Objects.requireNonNull(menuItem, "Menu item is required");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}