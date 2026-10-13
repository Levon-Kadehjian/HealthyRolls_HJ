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

    Order(UUID customerId, UUID restaurantId, List<OrderLine> items) {

        this.customerId = Objects.requireNonNull(customerId, "Customer ID is required");
        this.restaurantId = Objects.requireNonNull(restaurantId, "Restaurant ID is required");

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

    Order(OrderRecord record, OrderStateFactory stateFactory) {
        this(record.customerId(), record.restaurantId(), record.items());
        this.id = new OrderId(Objects.requireNonNull(record.orderId()));
        this.courierId = record.courierId();
        this.acceptedAt = record.acceptedAt();
        this.state = stateFactory.fromStatus(OrderStatus.valueOf(record.status()), this);
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public UUID getCourierId() {
        return courierId;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public List<OrderLine> getItems() {
        return items;
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
