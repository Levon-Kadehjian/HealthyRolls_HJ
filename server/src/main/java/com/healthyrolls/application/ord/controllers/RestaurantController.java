package com.healthyrolls.application.ord.controllers;

import com.healthyrolls.application.ord.MenuService;
import com.healthyrolls.application.ord.RestaurantService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.restaurant.Restaurant;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    public RestaurantController(RestaurantService restaurantService, MenuService menuService) {
        this.restaurantService = restaurantService;
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> listRestaurants(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        List<RestaurantResponse> restaurants = restaurantService.listRestaurants(user).stream()
                .map(RestaurantResponse::from)
                .toList();

        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResponse> getRestaurant(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID restaurantId) {

        Restaurant restaurant = restaurantService.getForBrowsing(restaurantId, user);

        return ResponseEntity.ok(RestaurantResponse.from(restaurant));
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemResponse>> browseMenu(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID restaurantId) {

        List<MenuItemResponse> items = menuService.browseMenu(restaurantId, user).getItems().stream()
                .map(MenuItemResponse::from)
                .toList();

        return ResponseEntity.ok(items);
    }
}
