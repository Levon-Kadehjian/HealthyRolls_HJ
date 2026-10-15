package com.healthyrolls.technicalservices.persistence.ord;

import java.util.Optional;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;

public interface OrderRepository {

    Order save(Order order);

    Order findById(OrderId id);

    boolean hasPreparingOrder(UUID restaurantId);

    Optional<Order> findNextAccepted(UUID restaurantId);
}