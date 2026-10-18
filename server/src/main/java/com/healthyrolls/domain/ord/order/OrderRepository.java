package com.healthyrolls.domain.ord.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Order findByOrderId(OrderId id);

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findByRestaurantId(UUID restaurantId);

    List<Order> findAll();

    boolean hasPreparingOrder(UUID restaurantId);

    Optional<Order> findNextAccepted(UUID restaurantId);
}
