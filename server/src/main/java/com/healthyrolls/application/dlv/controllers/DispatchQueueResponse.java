package com.healthyrolls.application.dlv.controllers;

import com.healthyrolls.domain.ord.order.OrderId;

import java.util.List;
import java.util.UUID;

public record DispatchQueueResponse(int size, List<UUID> orderIds) {

    public static DispatchQueueResponse from(List<OrderId> queue) {
        List<UUID> ids = queue.stream().map(OrderId::value).toList();
        return new DispatchQueueResponse(ids.size(), ids);
    }
}
