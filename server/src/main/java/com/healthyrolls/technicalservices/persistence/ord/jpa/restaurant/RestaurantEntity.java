package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
public class RestaurantEntity {

    @Id
    private UUID restaurantId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    protected RestaurantEntity() {
    }

    public RestaurantEntity(
            UUID restaurantId,
            String name,
            RestaurantStatus status,
            double latitude,
            double longitude) {

        this.restaurantId = restaurantId;
        this.name = name;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public RestaurantStatus getStatus() {
        return status;
    }
}