package com.healthyrolls.domain.ord.order;

import com.healthyrolls.domain.common.AuthenticatedUser;

public class OrderAccessPolicy {

    public boolean canView(AuthenticatedUser user, Order order) {
        return switch (user.role()) {
            case OPERATOR -> true;
            case CUSTOMER -> user.userId().value().equals(order.getCustomerId());
            case RESTAURANT_STAFF -> user.restaurantId() != null
                    && user.restaurantId().value().equals(order.getRestaurantId());
            case COURIER -> false;
        };
    }

    public boolean canPerform(AuthenticatedUser user, Order order, OrderAction action) {
        if (!canView(user, order)) {
            return false;
        }

        return switch (user.role()) {
            case CUSTOMER -> action == OrderAction.CANCEL && order.status() == OrderStatus.PLACED;
            case RESTAURANT_STAFF -> switch (action) {
                case ACCEPT, REJECT, START_PREPARING, MARK_READY -> true;
                default -> false;
            };
            case OPERATOR -> action == OrderAction.CANCEL;
            case COURIER -> false;
        };
    }
}
