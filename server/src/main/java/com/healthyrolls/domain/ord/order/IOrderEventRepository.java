package com.healthyrolls.domain.ord.order;

public interface IOrderEventRepository {

    void save(OrderStateChanged event);
}
