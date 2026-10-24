package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import com.healthyrolls.domain.ord.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {

    boolean existsByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);

    List<OrderEntity> findByCustomerId(UUID customerId);

    List<OrderEntity> findByRestaurantId(UUID restaurantId);

    Optional<OrderEntity> findFirstByRestaurantIdAndStatusOrderByAcceptedAtAscOrderIdAsc(
            UUID restaurantId, OrderStatus status);
}
