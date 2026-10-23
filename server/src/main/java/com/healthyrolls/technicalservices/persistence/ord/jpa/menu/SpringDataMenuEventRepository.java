package com.healthyrolls.technicalservices.persistence.ord.jpa.menu;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMenuEventRepository extends JpaRepository<MenuEventEntity, UUID> {

    List<MenuEventEntity> findByRestaurantIdOrderByOccurredAt(UUID restaurantId);
}
