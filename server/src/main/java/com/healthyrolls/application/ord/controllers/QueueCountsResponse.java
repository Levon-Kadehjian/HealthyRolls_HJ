package com.healthyrolls.application.ord.controllers;

import java.util.UUID;

import com.healthyrolls.domain.ord.order.QueueCounts;

public record QueueCountsResponse(UUID restaurantId, long queuedCount, long preparingCount, long readyCount) {
    public static QueueCountsResponse from(QueueCounts c) {
        return new QueueCountsResponse(c.restaurantId(), c.queued(), c.preparing(), c.ready());
    }
}
