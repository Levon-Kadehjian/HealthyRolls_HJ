package com.healthyrolls.technicalservices.persistence.ord;

import com.healthyrolls.domain.ord.order.OrderStateChanged;

public interface OrderEventRepository {

    void save(OrderStateChanged event);
}
