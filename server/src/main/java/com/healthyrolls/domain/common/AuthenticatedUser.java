package com.healthyrolls.domain.common;

public record AuthenticatedUser(UserId userId, UserRole role, RestaurantId restaurantId) {

    public static final String REQUEST_ATTRIBUTE = "healthyrolls.currentUser";

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    public boolean isStaff() {
        return role == UserRole.RESTAURANT_STAFF;
    }

    public boolean isCourier() {
        return role == UserRole.COURIER;
    }

    public boolean isOperator() {
        return role == UserRole.OPERATOR;
    }
}
