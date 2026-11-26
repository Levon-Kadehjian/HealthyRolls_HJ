package com.healthyrolls.application.ntf;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ntf.INotificationRepository;
import com.healthyrolls.domain.ntf.Notification;
import com.healthyrolls.domain.ntf.NotificationId;
import com.healthyrolls.domain.ntf.NotificationType;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

@Service
public class NotificationService {

    private final INotificationRepository notifications;
    private final IOrderRepository orders;

    public NotificationService(INotificationRepository notifications, IOrderRepository orders) {
        this.notifications = Objects.requireNonNull(notifications, "Notification repository is required");
        this.orders = Objects.requireNonNull(orders, "Order repository is required");
    }

    @EventListener
    @Transactional
    public void onOrderStateChanged(OrderStateChanged event) {
        Order order = orders.findByOrderId(event.orderId());
        notifications.save(new Notification(
                NotificationId.random(),
                new UserId(order.getCustomerId()),
                event.orderId(),
                NotificationType.ORDER_STATUS,
                messageFor(event),
                event.occurredAt()));
    }

    @Transactional
    public void alertOperators(OrderId orderId, Duration waited) {
        notifications.save(new Notification(
                NotificationId.random(),
                null,
                orderId,
                NotificationType.DISPATCH_ALERT,
                "Order " + orderId.value() + " has been waiting for a courier for "
                        + waited.toMinutes() + " min",
                Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<Notification> listFor(AuthenticatedUser user) {
        if (user.isOperator()) {
            return notifications.findOperatorAlerts();
        }
        return notifications.findByRecipient(user.userId());
    }

    @Transactional
    public void markRead(AuthenticatedUser user, NotificationId id) {
        notifications.findById(id)
                .filter(n -> canSee(user, n))
                .ifPresent(n -> {
                    n.markRead();
                    notifications.save(n);
                });
    }

    private boolean canSee(AuthenticatedUser user, Notification n) {
        if (n.getType() == NotificationType.DISPATCH_ALERT) {
            return user.isOperator();
        }
        return user.userId().equals(n.getRecipientId());
    }

    private String messageFor(OrderStateChanged event) {
        return "Your order is now " + event.toStatus().name().toLowerCase().replace('_', ' ');
    }
}
