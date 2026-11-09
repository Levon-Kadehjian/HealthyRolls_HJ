package com.healthyrolls.application.dlv.controllers;

import java.util.UUID;

import com.healthyrolls.application.dlv.StalledOrderReassignment;
import com.healthyrolls.domain.dlv.courier.Courier;

public record ReassignmentResponse(
        UUID orderId,
        UUID previousCourierId,
        String previousCourierName,
        UUID offeredCourierId,
        String offeredCourierName,
        boolean queued) {

    public static ReassignmentResponse from(StalledOrderReassignment result) {
        Courier offered = result.offeredCourier();
        return new ReassignmentResponse(
                result.orderId().value(),
                result.previousCourier().getId().value(),
                result.previousCourier().getName(),
                offered == null ? null : offered.getId().value(),
                offered == null ? null : offered.getName(),
                result.queued());
    }
}
