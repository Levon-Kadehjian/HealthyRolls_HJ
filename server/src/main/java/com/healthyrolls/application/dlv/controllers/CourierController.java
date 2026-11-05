package com.healthyrolls.application.dlv.controllers;

import com.healthyrolls.application.dlv.CourierService;
import com.healthyrolls.domain.common.AuthenticatedUser;

import org.springframework.http.ResponseEntity;
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

    @PostMapping("/me/on-duty")
    public ResponseEntity<Void> goOnDuty(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        courierService.goOnDuty(user.userId(), user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/off-duty")
    public ResponseEntity<Void> goOffDuty(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        courierService.goOffDuty(user.userId(), user);
        return ResponseEntity.noContent().build();
    }
}
