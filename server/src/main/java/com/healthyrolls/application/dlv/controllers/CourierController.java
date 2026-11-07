package com.healthyrolls.application.dlv.controllers;

import java.util.UUID;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.application.dlv.CourierService;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.application.ord.controllers.OrderResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/couriers")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @GetMapping("/me")
    public ResponseEntity<CourierResponse> getCourier(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        Courier courier = courierService.getCourier(user.userId(), user);

        boolean offerAccepted = courier.getStatus() == CourierStatus.ASSIGNED
                || courier.getStatus() == CourierStatus.CARRYING;

        return ResponseEntity.ok(CourierResponse.from(courier, offerAccepted));
    }

    @PostMapping("/me/on-duty")
    public ResponseEntity<CourierResponse> goOnDuty(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        Courier courier = courierService.goOnDuty(user.userId(), user);
        boolean offerAccepted = courier.getStatus() == CourierStatus.ASSIGNED;
        return ResponseEntity.ok(CourierResponse.from(courier, offerAccepted));
    }

    @PostMapping("/me/off-duty")
    public ResponseEntity<CourierResponse> goOffDuty(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        Courier courier = courierService.goOffDuty(user.userId(), user);
        return ResponseEntity.ok(CourierResponse.from(courier, false));
    }

    @PostMapping("/me/orders/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOffer(
            @PathVariable UUID orderId,
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        Order order = courierService.acceptOffer(user.userId(), new OrderId(orderId), user);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/me/orders/{orderId}/decline")
    public ResponseEntity<CourierResponse> declineOffer(
            @PathVariable UUID orderId,
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        Courier courier = courierService.declineOffer(user.userId(), new OrderId(orderId), user);
        return ResponseEntity.ok(CourierResponse.from(courier, false));
    }

    @GetMapping("/me/offer")
    public ResponseEntity<DeliveryOfferResponse> getPendingOffer(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        return courierService.getPendingOffer(user.userId(), user)
                .map(DeliveryOfferResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/me/orders/{orderId}/pickup")
    public ResponseEntity<OrderResponse> confirmPickup(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID orderId) {

        Order order = courierService.confirmPickup(
                user.userId(),
                new OrderId(orderId),
                user);

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/me/orders/{orderId}/deliver")
    public ResponseEntity<OrderResponse> confirmDelivery(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID orderId) {

        Order order = courierService.confirmDelivery(
                user.userId(),
                new OrderId(orderId),
                user);

        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
