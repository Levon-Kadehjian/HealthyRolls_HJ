package com.healthyrolls.domain.ord.event;

import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

import java.util.List;

public interface IEventRepository {

    void save(OrderStateChanged event);

    List<OrderStateChanged> findByOrderId(OrderId id);
}
