package com.healthyrolls.application.dash.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthyrolls.application.ord.OrderService;
import com.healthyrolls.application.ord.RestaurantService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.order.QueueCounts;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public MapController(RestaurantService restaurantService, OrderService orderService) {
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<MapRestaurantResponse>> restaurants(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        Map<UUID, QueueCounts> counts = orderService.queueCounts(user).stream()
                .collect(Collectors.toMap(QueueCounts::restaurantId, Function.identity()));

        return ResponseEntity.ok(restaurantService.listForMap(user).stream()
                .map(restaurant -> MapRestaurantResponse.from(restaurant, counts.get(restaurant.getRestaurantId())))
                .toList());
    }
}
