package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRestaurantEventRepository extends JpaRepository<RestaurantEventEntity, UUID> {
}
