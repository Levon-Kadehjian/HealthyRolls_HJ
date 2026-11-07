package com.healthyrolls.application.dlv.controllers;

import java.util.UUID;

import com.healthyrolls.domain.dlv.courier.Courier;

public record StalledOrderResponse(
        UUID orderId,
        UUID courierId,
        String courierName) {

    public static StalledOrderResponse from(Courier courier) {
        return new StalledOrderResponse(
                courier.getCurrentOrderId().value(),
                courier.getId().value(),
                courier.getName());
    }
}
