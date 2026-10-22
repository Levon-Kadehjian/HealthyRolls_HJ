package com.healthyrolls.domain.ord.order;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    private String rejectionReason;

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
        this.state = PlacedState.INSTANCE;
    }

    Order(OrderRecord record, OrderStateFactory stateFactory) {
        this(record.customerId(), record.restaurantId(), record.items());
        this.id = new OrderId(Objects.requireNonNull(record.orderId()));
        this.courierId = record.courierId();
        this.acceptedAt = record.acceptedAt();
        this.state = stateFactory.fromStatus(OrderStatus.valueOf(record.status()));
        this.rejectionReason = record.rejectionReason();
        this.version = record.version();
    }

    public OrderId getOrderId() {
        return id;
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

    public Long version() {
        return version;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void assignCourier(UUID id) {
        throw new UnsupportedOperationException();
    }

    public void apply(OrderAction action) {
        apply(action, null);
    }

    public void apply(OrderAction action, String reason) {
        Objects.requireNonNull(action, "Order action is required");

        switch (action) {
            case ACCEPT -> state.accept(this);

            case REJECT -> {
                if (reason == null || reason.isBlank()) {
                    throw new IllegalArgumentException(
                            "A rejection reason is required");
                }

                state.reject(this);
                rejectionReason = reason.strip();
            }

            case START_PREPARING -> state.startPreparing(this);
            case MARK_READY -> state.markReady(this);
            case PICK_UP -> state.markPickedUp(this);
            case DELIVER -> state.markDelivered(this);
            case CANCEL -> state.cancel(this);
            default -> throw new UnsupportedOperationException("Unsupported order action: " + action);
        }
    }

    public OrderStatus status() {
        return state.status();
    }

    void changeState(AbstractOrderState next) {
        Objects.requireNonNull(next, "Next state is required");

        if (next.status() == OrderStatus.ACCEPTED) {
            acceptedAt = Instant.now();
        }

        state = next;
    }
}
