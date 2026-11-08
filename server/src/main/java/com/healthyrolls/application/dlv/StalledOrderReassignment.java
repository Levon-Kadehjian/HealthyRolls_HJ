package com.healthyrolls.application.dlv;

import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.ord.order.OrderId;

public record StalledOrderReassignment(
        OrderId orderId,
        Courier previousCourier,
        Courier offeredCourier) {

    public boolean queued() {
        return offeredCourier == null;
    }
}
