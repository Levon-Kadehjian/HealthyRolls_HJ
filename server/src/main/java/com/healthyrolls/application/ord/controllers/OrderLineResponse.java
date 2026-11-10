package com.healthyrolls.application.ord.controllers;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineResponse(
        UUID menuItemId,
        String name,
        BigDecimal price,
        int quantity) {

    public OrderLineResponse(UUID menuItemId, int quantity) {
        this(menuItemId, null, null, quantity);
    }
}
