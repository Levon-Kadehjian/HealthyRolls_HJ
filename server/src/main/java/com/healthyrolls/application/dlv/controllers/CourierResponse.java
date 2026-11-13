package com.healthyrolls.application.dlv.controllers;

import java.util.UUID;

import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.ord.order.OrderId;

public record CourierResponse(
        UUID courierId,
        String name,
        CourierStatus status,
        double latitude,
        double longitude,
        UUID currentOrderId,
        boolean offerAccepted) {

    public static CourierResponse from(Courier c, boolean offerAccepted) {
        OrderId current = c.getCurrentOrderId();
        return new CourierResponse(
                c.getId().value(),
                c.getName(),
                c.getStatus(),
                c.getLatitude(),
                c.getLongitude(),
                current == null ? null : current.value(),
                offerAccepted);
    }
}
