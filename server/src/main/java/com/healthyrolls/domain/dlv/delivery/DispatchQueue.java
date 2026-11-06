package com.healthyrolls.domain.dlv.delivery;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.healthyrolls.domain.ord.order.OrderId;

public class DispatchQueue {

    private final Deque<OrderId> pending = new ArrayDeque<>();

    public synchronized void enqueue(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");

        if (!pending.contains(orderId)) {
            pending.addLast(orderId);
        }
    }

    public synchronized Optional<OrderId> poll() {
        return Optional.ofNullable(pending.pollFirst());
    }

    public synchronized boolean remove(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");
        return pending.remove(orderId);
    }

    public synchronized List<OrderId> snapshot() {
        return List.copyOf(pending);
    }

    public synchronized void clear() {
        pending.clear();
    }
}