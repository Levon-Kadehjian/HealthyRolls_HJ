package com.healthyrolls.technicalservices.persistence.jpa.menu;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMenuItemRepository
        extends JpaRepository<MenuItemEntity, UUID> {

    List<MenuItemEntity> findByRestaurant_RestaurantId(UUID restaurantId);
}