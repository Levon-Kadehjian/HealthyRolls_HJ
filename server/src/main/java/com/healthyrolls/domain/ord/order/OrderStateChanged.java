package com.healthyrolls.domain.ord.order;

import java.time.Instant;
import java.util.UUID;

public record OrderStateChanged(
        EventId eventId,
        OrderId orderId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        UUID userId,
        Instant occurredAt) {}