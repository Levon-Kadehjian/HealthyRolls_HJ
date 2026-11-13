package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import com.healthyrolls.domain.ord.order.OrderStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {

    boolean existsByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);

    @Query("select o.restaurantId, o.status, count(o) from OrderEntity o"
            + " where o.status in :statuses group by o.restaurantId, o.status")
    List<Object[]> countByRestaurantAndStatus(Collection<OrderStatus> statuses);

    List<OrderEntity> findByCustomerId(UUID customerId);

    List<OrderEntity> findByRestaurantId(UUID restaurantId);

    Optional<OrderEntity> findFirstByRestaurantIdAndStatusOrderByAcceptedAtAscOrderIdAsc(
            UUID restaurantId, OrderStatus status);
}
