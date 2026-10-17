package com.healthyrolls.domain.reg.seed;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserRole;

public record SeedUser(
        String email,
        String hashedPassword,
        String name,
        UserRole role,
        RestaurantId restaurantId) {}
