package com.healthyrolls.domain.order;

import java.util.List;

public class OrderService {

    private OrderRepository repository;
    private OrderFactory factory;
    private OrderEventRecorder recorder;

    public OrderId placeOrder(
            CustomerId customerId,
            RestaurantId restaurantId,
            List<OrderItem> items) {
        throw new UnsupportedOperationException();
    }

    public void changeOrder(
            OrderId orderId,
            OrderAction action,
            UserId userId) {
        throw new UnsupportedOperationException();
    }
}