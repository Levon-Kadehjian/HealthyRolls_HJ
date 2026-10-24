package com.healthyrolls.application.ord.controllers;

import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderEventResponse(
        UUID eventId,
        UUID orderId,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        UUID userId,
        Instant occurredAt) {

    public static OrderEventResponse from(OrderStateChanged o) {
        return new OrderEventResponse(
                o.eventId().value(),
                o.orderId().value(),
                o.fromStatus(),
                o.toStatus(),
                o.userId(),
                o.occurredAt());
    }
}