package com.healthyrolls.application.dash.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthyrolls.application.dash.ResetService;
import com.healthyrolls.domain.common.AuthenticatedUser;

@RestController
@RequestMapping("/api/admin")
public class ResetController {

    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        resetService.reset(user);
        return ResponseEntity.noContent().build();
    }
}