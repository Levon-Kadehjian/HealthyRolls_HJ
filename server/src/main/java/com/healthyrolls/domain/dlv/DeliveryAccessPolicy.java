package com.healthyrolls.domain.dlv;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.dlv.courier.Courier;

public class DeliveryAccessPolicy {

    public boolean canChangeDuty(AuthenticatedUser actor, Courier courier) {
        return actor.role() == UserRole.COURIER
                && actor.userId().equals(courier.getId());
    }
}
