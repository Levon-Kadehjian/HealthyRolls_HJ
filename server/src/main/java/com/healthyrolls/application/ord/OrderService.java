package com.healthyrolls.application.ord;

import java.util.List;
import java.util.UUID;

import com.healthyrolls.application.reg.UserContext;
import com.healthyrolls.domain.ord.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderEventRecorder;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.domain.ord.order.state.OrderId;
import com.healthyrolls.technicalservices.persistence.OrderRepository;

public class OrderService {

    private OrderRepository repository;
    private OrderFactory factory;
    private OrderEventRecorder recorder;

    public OrderId placeOrder(
            UserContext actor,
            UUID restaurantId,
            List<OrderLine> items) {
        throw new UnsupportedOperationException();
    }

    public void changeOrder(
            OrderId orderId,
            OrderAction action,
            UserContext actor) {
        throw new UnsupportedOperationException();
    }
}
