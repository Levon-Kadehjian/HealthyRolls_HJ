package com.healthyrolls.domain.ord.order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OrderLine(UUID menuItemId, int quantity, BigDecimal unitPrice) {

    public OrderLine {
        Objects.requireNonNull(menuItemId, "Menu item ID is required");
        Objects.requireNonNull(unitPrice, "Unit price is required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
}
