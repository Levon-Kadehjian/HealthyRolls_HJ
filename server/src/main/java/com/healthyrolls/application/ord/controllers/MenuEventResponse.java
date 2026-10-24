package com.healthyrolls.application.ord.controllers;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.menu.MenuItemStateChanged;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;

public record MenuEventResponse(
        UUID eventId,
        UUID itemId,
        UUID restaurantId,
        MenuItemStatus fromStatus,
        MenuItemStatus toStatus,
        UUID userId,
        Instant occurredAt) {

    public static MenuEventResponse from(MenuItemStateChanged e) {
        return new MenuEventResponse(
                e.eventId(),
                e.itemId(),
                e.restaurantId(),
                e.fromStatus(),
                e.toStatus(),
                e.userId(),
                e.occurredAt());
    }
}
