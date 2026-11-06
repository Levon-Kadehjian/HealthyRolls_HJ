package com.healthyrolls.domain.dlv;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.ord.order.OrderId;

public class DeliveryAccessPolicy {

    public boolean isOwnCourier(
            AuthenticatedUser actor,
            UserId courierId) {

        return actor.role() == UserRole.COURIER
                && actor.userId().equals(courierId);
    }

    public boolean canView(AuthenticatedUser actor, Courier courier) {
        return actor.role() == UserRole.OPERATOR
                || (actor.role() == UserRole.COURIER
                        && actor.userId().equals(courier.getId()));
    }

    public boolean canConfirmPickup(
            AuthenticatedUser actor,
            Courier courier,
            OrderId orderId) {

        return actor.role() == UserRole.COURIER
                && actor.userId().equals(courier.getId())
                && orderId.equals(courier.getCurrentOrderId());
    }

    public boolean canConfirmDelivery(
            AuthenticatedUser actor,
            Courier courier,
            OrderId orderId) {

        return isOwnCourier(actor, courier.getId())
                && orderId.equals(courier.getCurrentOrderId());
    }
}
