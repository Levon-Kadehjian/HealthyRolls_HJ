package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import com.healthyrolls.domain.ord.order.OrderStatus;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {

    boolean existsByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);

    Optional<OrderEntity> findFirstByRestaurantIdAndStatusOrderByAcceptedAtAscOrderIdAsc(
            UUID restaurantId, OrderStatus status);
}