package com.healthyrolls.application.dlv.controllers;

import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.application.dlv.CourierService;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.common.AuthenticatedUser;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

        return ResponseEntity.ok(
                CourierResponse.from(courier, offerAccepted));
    }

    @PostMapping("/me/on-duty")
    public ResponseEntity<CourierResponse> goOnDuty(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        Courier courier = courierService.goOnDuty(user.userId(), user);
        return ResponseEntity.ok(CourierResponse.from(courier, false));
    }

    @PostMapping("/me/off-duty")
    public ResponseEntity<CourierResponse> goOffDuty(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        Courier courier = courierService.goOffDuty(user.userId(), user);
        return ResponseEntity.ok(CourierResponse.from(courier, false));
    }
}
