package com.healthyrolls.domain.ntf;

import java.time.Instant;
import java.util.Objects;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ord.order.OrderId;

public class Notification {

    private final NotificationId id;
    private final UserId recipientId;
    private final OrderId orderId;
    private final NotificationType type;
    private final String message;
    private final Instant createdAt;
    private boolean read;

    public Notification(
            NotificationId id,
            UserId recipientId,
            OrderId orderId,
            NotificationType type,
            String message,
            Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Notification ID is required");
        this.recipientId = Objects.requireNonNull(recipientId, "Recipient ID is required");
        this.orderId = orderId;
        this.type = Objects.requireNonNull(type, "Notification type is required");
        this.message = Objects.requireNonNull(message, "Message is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp is required");
        this.read = false;
    }

    public NotificationId getId() {
        return id;
    }

    public UserId getRecipientId() {
        return recipientId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        this.read = true;
    }
}
