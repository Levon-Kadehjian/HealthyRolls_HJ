package com.healthyrolls.technicalservices.configuration;

import java.math.BigDecimal;
import java.util.UUID;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;

public record SeedMenuItem(
        UUID itemId,
        String name,
        String description,
        BigDecimal price,
        MenuItemStatus status) {}