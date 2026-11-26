package com.healthyrolls.application.ntf;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.healthyrolls.application.dlv.DispatchExtension;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.dlv.delivery.DispatchQueue;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.technicalservices.persistence.ntf.NotificationProperties;

@Component
public class DispatchWaitMonitor implements DispatchExtension {

    private final Map<OrderId, Instant> waitingSince = new ConcurrentHashMap<>();
    private final Set<OrderId> alerted = ConcurrentHashMap.newKeySet();
    private final NotificationService notificationService;
    private final NotificationProperties properties;
    private final DispatchQueue queue;

    public DispatchWaitMonitor(
            NotificationService notificationService,
            NotificationProperties properties,
            DispatchQueue queue) {
        this.notificationService = Objects.requireNonNull(notificationService, "Notification service is required");
        this.properties = Objects.requireNonNull(properties, "Notification properties are required");
        this.queue = Objects.requireNonNull(queue, "Dispatch queue is required");
    }

    @Override
    public void onWaitingOrder(OrderId orderId, Instant since) {
        waitingSince.putIfAbsent(orderId, since);
    }

    @Override
    public void onAssignmentCreated(OrderId orderId, Assignment a) {
        waitingSince.remove(orderId);
        alerted.remove(orderId);
    }

    @Scheduled(fixedDelayString = "${notification.scan-interval}")
    public void checkWaitingOrders() {
        Set<OrderId> stillQueued = Set.copyOf(queue.snapshot());
        waitingSince.keySet().retainAll(stillQueued);
        alerted.retainAll(stillQueued);

        Instant now = Instant.now();
        Duration threshold = properties.getDispatchWaitThreshold();
        waitingSince.forEach((orderId, since) -> {
            Duration waited = Duration.between(since, now);
            if (waited.compareTo(threshold) >= 0 && alerted.add(orderId)) {
                notificationService.alertOperators(orderId, waited);
            }
        });
    }

    public void clear() {
        waitingSince.clear();
        alerted.clear();
    }
}
