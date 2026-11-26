package com.healthyrolls.technicalservices.persistence.ntf.jpa;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ntf.Notification;
import com.healthyrolls.domain.ntf.NotificationId;
import com.healthyrolls.domain.ntf.NotificationType;
import com.healthyrolls.domain.ord.order.OrderId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    private UUID id;

    private UUID orderId;

    private UUID recipientId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean read;

    protected NotificationEntity() {
    }

    static NotificationEntity from(Notification n) {
        NotificationEntity r = new NotificationEntity();
        r.id = n.getId().value();
        r.orderId = n.getOrderId() != null ? n.getOrderId().value() : null;
        r.recipientId = n.getRecipientId() != null ? n.getRecipientId().value() : null;
        r.type = n.getType().name();
        r.message = n.getMessage();
        r.createdAt = n.getCreatedAt();
        r.read = n.isRead();
        return r;
    }

    Notification toDomain() {
        Notification n = new Notification(
                new NotificationId(id),
                recipientId != null ? new UserId(recipientId) : null,
                orderId != null ? new OrderId(orderId) : null,
                NotificationType.valueOf(type),
                message,
                createdAt);
        if (read) {
            n.markRead();
        }
        return n;
    }
}
