package com.healthyrolls.technicalservices.persistence.ord;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;

public interface OrderRepository {

    Order save(Order order);

    Order findById(OrderId id);

    Order findNextAccepted();
}