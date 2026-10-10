package com.healthyrolls.domain.order;

import java.time.Instant;

public record OrderStateChanged(
        EventId eventId,
        OrderId orderId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        UserId userId,
        Instant occurredAt) {
}