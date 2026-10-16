package com.healthyrolls.domain.ord.order;

public interface OrderEventRepository {

    void save(OrderStateChanged event);
}
