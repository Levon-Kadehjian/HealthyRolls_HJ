package com.healthyrolls.domain.ord.order;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.Objects;

import com.healthyrolls.domain.ord.order.state.PlacedState;

public class Order {

    private OrderId id;
    private UUID customerId;
    private UUID restaurantId;
    private UUID courierId;
    private AbstractOrderState state;
    private Instant acceptedAt;
    private long version;
    private List<OrderLine> items;

    Order(
            UUID customerId,
            UUID restaurantId,
            List<OrderLine> items) {

        this.customerId = Objects.requireNonNull(
                customerId, "Customer ID is required");
        this.restaurantId = Objects.requireNonNull(
                restaurantId, "Restaurant ID is required");

        Objects.requireNonNull(items, "Order lines are required");

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "An order must contain at least one line");
        }

        this.items = List.copyOf(items);
        this.id = OrderId.random();
        this.state = new PlacedState(this);
    }

    public OrderId getOrderId() {
        return id;
    }

    public void assignCourier(UUID id) {
        throw new UnsupportedOperationException();
    }

    public void apply(OrderAction action) {
        throw new UnsupportedOperationException();
    }

    public OrderStatus status() {
        return state.status();
    }

    void changeState(AbstractOrderState next) {
        throw new UnsupportedOperationException();
    }
}