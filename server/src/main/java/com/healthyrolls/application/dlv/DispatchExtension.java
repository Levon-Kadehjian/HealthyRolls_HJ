package com.healthyrolls.application.dlv;

import java.time.Instant;

import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.ord.order.OrderId;

public interface DispatchExtension {

    void onWaitingOrder(OrderId orderId, Instant since);

    void onAssignmentCreated(OrderId orderId, Assignment a);
}
