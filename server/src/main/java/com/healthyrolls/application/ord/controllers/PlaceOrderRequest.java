package com.healthyrolls.application.ord.controllers;

import java.util.List;
import java.util.UUID;

public record PlaceOrderRequest(
        UUID restaurantId,
        List<OrderLineRequest> lines) {}