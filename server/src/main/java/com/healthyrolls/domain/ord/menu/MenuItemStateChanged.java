package com.healthyrolls.domain.ord.menu;

import java.time.Instant;
import java.util.UUID;

public record MenuItemStateChanged(
        UUID eventId,
        UUID itemId,
        UUID restaurantId,
        MenuItemStatus fromStatus,
        MenuItemStatus toStatus,
        UUID userId,
        Instant occurredAt) {}
