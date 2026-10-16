package com.healthyrolls.domain.ord.order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Order findById(OrderId id);

    boolean hasPreparingOrder(UUID restaurantId);

    Optional<Order> findNextAccepted(UUID restaurantId);
}