package com.healthyrolls.domain.dlv.delivery;

import java.time.Instant;
import java.util.Objects;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ord.order.OrderId;

public class Assignment {

    private final OrderId orderId;
    private final UserId courierId;
    private final Instant assignedAt;
    private Instant acceptedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private boolean active;

    public Assignment(OrderId orderId, UserId courierId) {
        this.orderId = Objects.requireNonNull(orderId, "Order ID is required");
        this.courierId = Objects.requireNonNull(courierId, "Courier ID is required");
        this.assignedAt = Instant.now();
        this.active = true;
    }

    public Assignment(
            OrderId orderId,
            UserId courierId,
            Instant assignedAt,
            Instant acceptedAt,
            Instant pickedUpAt,
            Instant deliveredAt,
            boolean active) {

        this.orderId = Objects.requireNonNull(orderId, "Order ID is required");
        this.courierId = Objects.requireNonNull(courierId, "Courier ID is required");
        this.assignedAt = Objects.requireNonNull(
                assignedAt, "Assignment time is required");
        this.acceptedAt = acceptedAt;
        this.pickedUpAt = pickedUpAt;
        this.deliveredAt = deliveredAt;
        this.active = active;
    }

    public void accept() {
        if (!active) {
            throw new IllegalStateException(
                    "An inactive assignment cannot be accepted");
        }
        if (acceptedAt != null) {
            throw new IllegalStateException(
                    "This offer has already been accepted");
        }

        acceptedAt = Instant.now();
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getCourierId() {
        return courierId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getPickedUpAt() {
        return pickedUpAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public boolean isActive() {
        return active;
    }
}