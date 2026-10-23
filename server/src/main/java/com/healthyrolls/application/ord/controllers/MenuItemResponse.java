package com.healthyrolls.application.ord.controllers;

import java.math.BigDecimal;
import java.util.UUID;
import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;

public record MenuItemResponse(UUID itemId, String name, BigDecimal price, MenuItemStatus status) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(item.getItemId(), item.getName(), item.getPrice(), item.getStatus());
    }
}
