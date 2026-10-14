package com.healthyrolls.domain.common;

public record AuthenticatedUser(UserId userId, UserRole role, RestaurantId restaurantId) {

    public static final String REQUEST_ATTRIBUTE = "healthyrolls.currentUser";
}
