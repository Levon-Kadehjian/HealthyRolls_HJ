package com.healthyrolls.technicalservices.persistence.jpa.event;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_events")
public class OrderEventEntity {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant occurredAt;

    protected OrderEventEntity() {
    }

    public OrderEventEntity(OrderStateChanged event) {
        this.eventId = event.eventId().value();
        this.orderId = event.orderId().value();
        this.fromStatus = event.fromStatus();
        this.toStatus = event.toStatus();
        this.userId = event.userId();
        this.occurredAt = event.occurredAt();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderStatus getFromStatus() {
        return fromStatus;
    }

    public OrderStatus getToStatus() {
        return toStatus;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
