package com.healthyrolls.domain.ord.order;

import java.util.List;

public interface IOrderEventRepository {

    void save(OrderStateChanged event);

    List<OrderStateChanged> findByOrderId(OrderId id);
}
