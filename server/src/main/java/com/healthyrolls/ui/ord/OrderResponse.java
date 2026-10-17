package com.healthyrolls.ui.ord;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        OrderStatus status,
        List<OrderLineResponse> lines,
        String rejectionReason,
        Instant acceptedAt) {

    public static OrderResponse from(Order o) {
        List<OrderLineResponse> lines = o.getItems().stream()
                .map(line -> new OrderLineResponse(line.menuItem().getItemId(), line.quantity()))
                .toList();

        return new OrderResponse(
                o.getOrderId().value(),
                o.getCustomerId(),
                o.getRestaurantId(),
                o.status(),
                lines,
                o.getRejectionReason(),
                o.getAcceptedAt());
    }
}