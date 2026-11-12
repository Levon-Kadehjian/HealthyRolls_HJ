package com.healthyrolls.domain.reg.seed;

import java.util.UUID;

import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserRole;

public record SeedUser(
        UUID id,
        String email,
        String hashedPassword,
        String name,
        UserRole role,
        RestaurantId restaurantId,
        Double latitude,
        Double longitude,
        CourierStatus courierStatus) {}
