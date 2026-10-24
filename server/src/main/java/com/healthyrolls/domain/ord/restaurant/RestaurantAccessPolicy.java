package com.healthyrolls.domain.ord.restaurant;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;

import java.util.UUID;

public class RestaurantAccessPolicy {

    public boolean canReopen(AuthenticatedUser user) {
        return user.role() == UserRole.OPERATOR;
    }

    public boolean canManage(AuthenticatedUser user, UUID restaurantId) {
        return switch (user.role()) {
            case OPERATOR -> true;
            case RESTAURANT_STAFF -> user.restaurantId() != null
                    && user.restaurantId().value().equals(restaurantId);
            case CUSTOMER, COURIER -> false;
        };
    }
}
