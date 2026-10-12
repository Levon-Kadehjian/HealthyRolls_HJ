package com.healthyrolls.technicalservices.persistence;

import com.healthyrolls.domain.ord.Order;
import com.healthyrolls.domain.ord.OrderId;

public interface OrderRepository {

    Order save(Order order);

    Order findById(OrderId id);

    Order findNextAccepted();
}