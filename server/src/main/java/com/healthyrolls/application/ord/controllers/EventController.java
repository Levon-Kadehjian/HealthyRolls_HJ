package com.healthyrolls.application.ord.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthyrolls.application.ord.MenuEventService;
import com.healthyrolls.application.ord.OrderEventService;
import com.healthyrolls.application.ord.RestaurantEventService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.order.OrderId;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final OrderEventService orderEventService;
    private final RestaurantEventService restaurantEventService;
    private final MenuEventService menuEventService;

    public EventController(
            OrderEventService orderEventService,
            RestaurantEventService restaurantEventService,
            MenuEventService menuEventService) {

        this.orderEventService = orderEventService;
        this.restaurantEventService = restaurantEventService;
        this.menuEventService = menuEventService;
    }

    @GetMapping
    public ResponseEntity<List<OrderEventResponse>> historyOf(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        List<OrderEventResponse> events = orderEventService.historyOf(user).stream()
                .map(OrderEventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<OrderEventResponse>> historyOfOrder(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID orderId) {

        List<OrderEventResponse> events = orderEventService.historyOfOrder(user, new OrderId(orderId)).stream()
                .map(OrderEventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<List<RestaurantEventResponse>> historyOfRestaurant(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID restaurantId) {

        List<RestaurantEventResponse> events = restaurantEventService.historyOf(user, restaurantId).stream()
                .map(RestaurantEventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }

    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<MenuEventResponse>> historyOfMenu(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID restaurantId) {

        List<MenuEventResponse> events = menuEventService.historyOf(user, restaurantId).stream()
                .map(MenuEventResponse::from)
                .toList();

        return ResponseEntity.ok(events);
    }
}
