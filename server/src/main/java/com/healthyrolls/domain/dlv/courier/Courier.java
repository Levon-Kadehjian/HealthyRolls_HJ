package com.healthyrolls.domain.dlv.courier;

import java.util.Objects;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.exceptions.CourierCarryingOrderException;
import com.healthyrolls.domain.dlv.exceptions.InvalidCourierTransitionException;
import com.healthyrolls.domain.ord.order.OrderId;

public class Courier {

    // used to calculate straight-line distance between 2 coordinates
    private static final double EARTH_RADIUS_KM = 6371.0;

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
        boolean holdsOrder = status == CourierStatus.ASSIGNED || status == CourierStatus.CARRYING;
        boolean mayHoldOrder = holdsOrder || status == CourierStatus.OFFLINE;
        if ((holdsOrder && currentOrderId == null) || (!mayHoldOrder && currentOrderId != null)) {
            throw new IllegalArgumentException(
                    "A courier has a current order only while ASSIGNED, CARRYING or OFFLINE");
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
        if (status != CourierStatus.ASSIGNED) {
            require(CourierStatus.IDLE, "go off duty");
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

        if (status == CourierStatus.OFFLINE) {
            currentOrderId = null;
            return;
        }

        require(CourierStatus.ASSIGNED, "release a cancelled order");

        currentOrderId = null;
        status = CourierStatus.IDLE;
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

    // source: https://stackoverflow.com/a/16794680
    public double distanceTo(double lat, double lon) {
        double deltaLat = Math.toRadians(lat - latitude);
        double deltaLon = Math.toRadians(lon - longitude);
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(lat))
                        * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    private void require(CourierStatus expected, String action) {
        if (status != expected) {
            throw new InvalidCourierTransitionException(
                    "Courier " + id.value() + " cannot " + action + " while " + status);
        }
    }
}
