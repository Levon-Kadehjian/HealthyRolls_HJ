package com.healthyrolls.ui.ord;

import java.util.List;
import java.util.UUID;

public record PlaceOrderRequest(
        UUID restaurantId,
        List<OrderLineRequest> lines) {}