package com.healthyrolls.domain.order;

public interface OrderRepository {

    Order save(Order order);

    Order findById(OrderId id);

    Order findNextAccepted();
}