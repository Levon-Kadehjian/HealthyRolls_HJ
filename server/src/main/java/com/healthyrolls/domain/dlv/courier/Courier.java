package com.healthyrolls.domain.dlv.courier;

import java.util.Objects;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.exceptions.CourierCarryingOrderException;
import com.healthyrolls.domain.dlv.exceptions.InvalidCourierTransitionException;
import com.healthyrolls.domain.ord.order.OrderId;

public class Courier {

    private final UserId id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private CourierStatus status;
    private OrderId currentOrderId;

    public Courier(UserId id, String name, double latitude, double longitude,
            CourierStatus status, OrderId currentOrderId) {

        this.id = Objects.requireNonNull(id, "Courier ID is required");
        this.name = Objects.requireNonNull(name, "Courier name is required");
        this.status = Objects.requireNonNull(status, "Courier status is required");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Courier name cannot be blank");
        }
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        boolean requiresOrder = status == CourierStatus.ASSIGNED || status == CourierStatus.CARRYING;

        if (requiresOrder && currentOrderId == null) {
            throw new IllegalArgumentException("An assigned or carrying courier must have a current order");
        }

        if (status == CourierStatus.IDLE && currentOrderId != null) {
            throw new IllegalArgumentException("An idle courier cannot have a current order");
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.currentOrderId = currentOrderId;
    }

    public UserId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public CourierStatus getStatus() {
        return status;
    }

    public OrderId getCurrentOrderId() {
        return currentOrderId;
    }

    public boolean isAvailable() {
        return status == CourierStatus.IDLE;
    }

    public void goOnDuty() {
        require(CourierStatus.OFFLINE, "go on duty");
        status = currentOrderId == null ? CourierStatus.IDLE : CourierStatus.ASSIGNED;
    }

    public void goOffDuty() {
        if (status == CourierStatus.CARRYING) {
            throw new CourierCarryingOrderException(id);
        }

        if (status != CourierStatus.IDLE
                && status != CourierStatus.ASSIGNED) {
            throw new InvalidCourierTransitionException(
                    "Courier cannot go off duty while " + status);
        }

        status = CourierStatus.OFFLINE;
    }

    public void assign(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");
        require(CourierStatus.IDLE, "be assigned an order");
        status = CourierStatus.ASSIGNED;
        currentOrderId = orderId;
    }

    public void confirmPickup() {
        require(CourierStatus.ASSIGNED, "confirm pickup");
        status = CourierStatus.CARRYING;
    }

    public void confirmDelivery() {
        require(CourierStatus.CARRYING, "confirm delivery");
        status = CourierStatus.IDLE;
        currentOrderId = null;
    }

    public void releaseCancelledOrder(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");

        if (!orderId.equals(currentOrderId)) {
            throw new InvalidCourierTransitionException(
                    "This is not the courier's current order");
        }

        if (status != CourierStatus.ASSIGNED
                && status != CourierStatus.OFFLINE) {
            throw new InvalidCourierTransitionException(
                    "Cannot release a cancelled order while " + status);
        }

        currentOrderId = null;

        if (status == CourierStatus.ASSIGNED) {
            status = CourierStatus.IDLE;
        }
    }

    public void releaseStalledOrder(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");

        if (!orderId.equals(currentOrderId)) {
            throw new InvalidCourierTransitionException(
                    "This is not the courier's current order");
        }

        require(CourierStatus.OFFLINE, "release a stalled order");

        currentOrderId = null;
    }

    public double distanceTo(double lat, double lon) {
        double latitudeDifference = this.latitude - lat;
        double longitudeDifference = this.longitude - lon;

        return Math.hypot(latitudeDifference, longitudeDifference);
    }

    private void require(CourierStatus expected, String action) {
        if (status != expected) {
            throw new InvalidCourierTransitionException(
                    "Courier " + id.value() + " cannot " + action + " while " + status);
        }
    }
}
