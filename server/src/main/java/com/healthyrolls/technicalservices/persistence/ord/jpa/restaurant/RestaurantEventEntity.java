package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "restaurant_events")
public class RestaurantEventEntity {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus toStatus;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant occurredAt;

    protected RestaurantEventEntity() {
    }

    public RestaurantEventEntity(RestaurantStateChanged event) {
        this.eventId = event.eventId();
        this.restaurantId = event.restaurantId();
        this.fromStatus = event.fromStatus();
        this.toStatus = event.toStatus();
        this.userId = event.userId();
        this.occurredAt = event.occurredAt();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public RestaurantStatus getFromStatus() {
        return fromStatus;
    }

    public RestaurantStatus getToStatus() {
        return toStatus;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
