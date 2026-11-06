package com.healthyrolls.domain.dlv.courier;

import java.time.Instant;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.OrderId;

public record CourierStateChanged(
        EventId eventId,
        UserId courierId,
        OrderId orderId,
        CourierStatus fromStatus,
        CourierStatus toStatus,
        Instant occurredAt) {}
