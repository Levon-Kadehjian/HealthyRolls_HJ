package com.healthyrolls.technicalservices.persistence.ord.jpa.menu;

import com.healthyrolls.domain.ord.menu.MenuItemStateChanged;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "menu_events")
public class MenuEventEntity {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemStatus toStatus;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant occurredAt;

    protected MenuEventEntity() {
    }

    public MenuEventEntity(MenuItemStateChanged event) {
        this.eventId = event.eventId();
        this.itemId = event.itemId();
        this.restaurantId = event.restaurantId();
        this.fromStatus = event.fromStatus();
        this.toStatus = event.toStatus();
        this.userId = event.userId();
        this.occurredAt = event.occurredAt();
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public MenuItemStatus getFromStatus() {
        return fromStatus;
    }

    public MenuItemStatus getToStatus() {
        return toStatus;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
