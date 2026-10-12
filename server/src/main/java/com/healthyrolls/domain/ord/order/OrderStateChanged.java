package com.healthyrolls.domain.ord.order;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.state.OrderId;

public record OrderStateChanged(
        EventId eventId,
        OrderId orderId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        UUID userId,
        Instant occurredAt) {
}