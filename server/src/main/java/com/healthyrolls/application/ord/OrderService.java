package com.healthyrolls.application.ord;

import java.util.List;
import java.util.UUID;

import com.healthyrolls.application.reg.UserContext;
import com.healthyrolls.domain.ord.OrderAction;
import com.healthyrolls.domain.ord.OrderEventRecorder;
import com.healthyrolls.domain.ord.OrderFactory;
import com.healthyrolls.domain.ord.OrderId;
import com.healthyrolls.domain.ord.OrderItem;
import com.healthyrolls.technicalservices.persistence.OrderRepository;

public class OrderService {

    private OrderRepository repository;
    private OrderFactory factory;
    private OrderEventRecorder recorder;

    public OrderId placeOrder(
            UserContext actor,
            UUID restaurantId,
            List<OrderItem> items) {
        throw new UnsupportedOperationException();
    }

    public void changeOrder(
            OrderId orderId,
            OrderAction action,
            UserContext actor) {
        throw new UnsupportedOperationException();
    }
}