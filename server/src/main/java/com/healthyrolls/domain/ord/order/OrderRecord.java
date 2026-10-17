package com.healthyrolls.domain.ord.order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderRecord(
        UUID orderId,
        String status,
        UUID customerId,
        UUID restaurantId,
        UUID courierId,
        Instant acceptedAt,
        List<OrderLine> items) {
    public OrderRecord {
        items = List.copyOf(items);
    }
}
