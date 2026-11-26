package com.healthyrolls.domain.ntf;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ord.order.OrderId;
import java.time.Instant;
import java.util.Objects;

public class Notification {

    private final NotificationId id;
    private final UserId recipientId;
    private final OrderId orderId;
    private final NotificationType type;
    private final String message;
    private final Instant createdAt;
    private boolean read;

    public Notification(NotificationId id, UserId recipientId, OrderId orderId,
            NotificationType type, String message, Instant createdAt) {

        this.id = Objects.requireNonNull(id, "Notification ID is required");
        this.type = Objects.requireNonNull(type, "Notification type is required");
        this.message = Objects.requireNonNull(message, "Notification message is required");
        this.createdAt = Objects.requireNonNull(createdAt, "Notification createdAt is required");
        this.recipientId = recipientId;
        this.orderId = orderId;
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
        read = true;
    }
}
