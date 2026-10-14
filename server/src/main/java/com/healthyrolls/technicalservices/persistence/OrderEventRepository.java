package com.healthyrolls.technicalservices.persistence;

import com.healthyrolls.domain.ord.order.OrderStateChanged;

public interface OrderEventRepository {

    void save(OrderStateChanged event);
}
