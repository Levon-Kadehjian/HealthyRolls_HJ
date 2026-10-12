package com.healthyrolls.technicalservices.persistence;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.state.OrderId;

public interface OrderRepository {

    Order save(Order order);

    Order findById(OrderId id);

    Order findNextAccepted();
}