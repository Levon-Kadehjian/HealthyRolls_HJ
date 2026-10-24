package com.healthyrolls.domain.ord.order;

public enum OrderStatus {
    NONE, // for when an order is placed: NONE -> PLACED
    PLACED,
    ACCEPTED,
    PREPARING,
    READY,
    PICKED_UP,
    DELIVERED,
    CANCELLED,
    REJECTED
}
