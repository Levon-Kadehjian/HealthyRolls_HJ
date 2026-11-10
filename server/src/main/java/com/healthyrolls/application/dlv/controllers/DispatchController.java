package com.healthyrolls.application.dlv.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthyrolls.application.dlv.CourierService;
import com.healthyrolls.application.dlv.StalledOrderReassignment;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.ord.order.OrderId;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final CourierService courierService;

    public DispatchController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping("/couriers")
    public ResponseEntity<List<CourierResponse>> getCouriers(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        List<CourierResponse> couriers = courierService.getOnDutyCouriers(user).stream()
                .map(courier -> CourierResponse.from(courier,
                        courier.getStatus() == CourierStatus.ASSIGNED
                                || courier.getStatus() == CourierStatus.CARRYING))
                .toList();

        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/stalled-orders")
    public ResponseEntity<List<StalledOrderResponse>> getStalledOrders(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        List<StalledOrderResponse> stalled = courierService.getStalledCouriers(user).stream()
                .map(StalledOrderResponse::from)
                .toList();

        return ResponseEntity.ok(stalled);
    }

    @PostMapping("/stalled-orders/{orderId}/reassign")
    public ResponseEntity<ReassignmentResponse> reassignStalledOrder(
            @PathVariable UUID orderId,
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        StalledOrderReassignment result = courierService.reassignStalledOrder(new OrderId(orderId), user);
        return ResponseEntity.ok(ReassignmentResponse.from(result));
    }
}
