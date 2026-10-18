package com.healthyrolls.ui.ord;

import com.healthyrolls.application.ord.EventService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.order.OrderId;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<OrderEventResponse>> historyOf(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        List<OrderEventResponse> events = eventService.historyOf(user).stream()
                .map(OrderEventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<OrderEventResponse>> historyOfOrder(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID orderId) {

        List<OrderEventResponse> events = eventService.historyOfOrder(user, new OrderId(orderId)).stream()
                .map(OrderEventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }
}