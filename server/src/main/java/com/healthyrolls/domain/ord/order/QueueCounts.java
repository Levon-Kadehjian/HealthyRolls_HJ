package com.healthyrolls.domain.ord.order;

import java.util.UUID;

public record QueueCounts(UUID restaurantId, long queued, long preparing, long ready) {}
