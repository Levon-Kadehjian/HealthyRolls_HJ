package com.healthyrolls.technicalservices.persistence.jpa.menu;

import java.math.BigDecimal;
import java.util.UUID;

import com.healthyrolls.domain.ord.menu.MenuItemStatus;
import com.healthyrolls.technicalservices.persistence.jpa.restaurant.RestaurantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
public class MenuItemEntity {

    @Id
    private UUID itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemStatus status;

    protected MenuItemEntity() {
    }

    public MenuItemEntity(
            UUID itemId,
            RestaurantEntity restaurant,
            String name,
            BigDecimal price,
            MenuItemStatus status) {

        this.itemId = itemId;
        this.restaurant = restaurant;
        this.name = name;
        this.price = price;
        this.status = status;
    }

    public UUID getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public MenuItemStatus getStatus() {
        return status;
    }
}