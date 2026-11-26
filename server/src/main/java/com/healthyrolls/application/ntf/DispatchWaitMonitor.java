package com.healthyrolls.application.ntf;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.healthyrolls.application.dlv.DispatchExtension;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.ord.order.OrderId;

@Component
public class DispatchWaitMonitor implements DispatchExtension {

    private final Map<OrderId, Instant> waitingSince = new ConcurrentHashMap<>();
    private final Set<OrderId> alerted = ConcurrentHashMap.newKeySet();

    @Override
    public void onWaitingOrder(OrderId orderId, Instant since) {
        waitingSince.putIfAbsent(orderId, since);
    }

    @Override
    public void onAssignmentCreated(OrderId orderId, Assignment a) {
        waitingSince.remove(orderId);
        alerted.remove(orderId);
    }

    @Scheduled(fixedDelayString = "${notifications.check-interval}")
    public void checkWaitingOrders() {
    }

    public void clear() {
        waitingSince.clear();
        alerted.clear();
    }
}
