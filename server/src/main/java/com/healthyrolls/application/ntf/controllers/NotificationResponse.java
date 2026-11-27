package com.healthyrolls.application.ntf.controllers;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ntf.Notification;
import com.healthyrolls.domain.ntf.NotificationType;

public record NotificationResponse(
        UUID id,
        UUID orderId,
        NotificationType type,
        String message,
        Instant createdAt,
        boolean read) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId().value(),
                n.getOrderId() != null ? n.getOrderId().value() : null,
                n.getType(),
                n.getMessage(),
                n.getCreatedAt(),
                n.isRead());
    }
}
