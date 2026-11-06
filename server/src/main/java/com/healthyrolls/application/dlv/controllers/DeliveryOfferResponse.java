package com.healthyrolls.application.dlv.controllers;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.dlv.delivery.Assignment;

public record DeliveryOfferResponse(
        UUID orderId,
        Instant offeredAt) {

    public static DeliveryOfferResponse from(Assignment assignment) {
        return new DeliveryOfferResponse(
                assignment.getOrderId().value(),
                assignment.getAssignedAt());
    }
}