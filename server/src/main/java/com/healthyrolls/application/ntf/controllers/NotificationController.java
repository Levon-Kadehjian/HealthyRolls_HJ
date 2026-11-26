package com.healthyrolls.application.ntf.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthyrolls.application.ntf.NotificationService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> listNotifications(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser actor) {
        if (actor == null || (!actor.isCustomer() && !actor.isOperator())) {
            throw new ActionNotPermittedException("User is not permitted to view notifications");
        }

        return service.listFor(actor).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
