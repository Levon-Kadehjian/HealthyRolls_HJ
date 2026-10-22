package com.healthyrolls.application.reg.controllers;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.User;

public record UserResponse(
        UserId id,
        String email,
        String name,
        UserRole role,
        RestaurantId restaurantId) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.name(),
                user.role(),
                user.restaurantId().orElse(null));
    }
}
