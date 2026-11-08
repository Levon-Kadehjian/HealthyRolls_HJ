package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.dlv.courier.CourierStateChanged;
import com.healthyrolls.domain.dlv.courier.CourierStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courier_events")
public class CourierEventEntity {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private UUID courierId;

    @Column
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierStatus toStatus;

    @Column(nullable = false)
    private Instant occurredAt;

    protected CourierEventEntity() {
    }

    public CourierEventEntity(CourierStateChanged event) {
        this.eventId = event.eventId().value();
        this.courierId = event.courierId().value();
        this.orderId = event.orderId() == null ? null : event.orderId().value();
        this.fromStatus = event.fromStatus();
        this.toStatus = event.toStatus();
        this.occurredAt = event.occurredAt();
    }
}
