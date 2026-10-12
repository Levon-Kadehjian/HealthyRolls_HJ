package com.healthyrolls.domain.ord;

import java.time.Instant;
import java.util.UUID;

public record OrderRecord(
                UUID orderId,
                String status,
                UUID customerId,
                UUID restaurantId,
                UUID courierId,
                Instant acceptedAt) {
}