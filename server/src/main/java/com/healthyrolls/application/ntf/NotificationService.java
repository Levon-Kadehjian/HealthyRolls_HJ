package com.healthyrolls.application.ntf;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ntf.INotificationRepository;
import com.healthyrolls.domain.ntf.Notification;
import com.healthyrolls.domain.ntf.NotificationId;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.reg.IUserRepository;

@Service
public class NotificationService {

    private final INotificationRepository notifications;
    private final IOrderRepository orders;
    private final IUserRepository users;

    @Autowired(required = false)
    public NotificationService(
            INotificationRepository notifications,
            IOrderRepository orders,
            IUserRepository users) {
        this.notifications = notifications;
        this.orders = orders;
        this.users = users;
    }

    public NotificationService() {
        this(null, null, null);
    }

    @EventListener
    public void onOrderStateChanged(OrderStateChanged event) {

    }

    public void alertOperators(OrderId orderId, Duration waited) {

    }

    public List<Notification> listFor(AuthenticatedUser user) {
        if (notifications == null || user == null) {
            return Collections.emptyList();
        }
        return notifications.findByRecipient(user.userId());
    }

    public void markRead(AuthenticatedUser user, NotificationId id) {
        if (notifications != null && id != null) {
            notifications.findById(id).ifPresent(Notification::markRead);
        }
    }

    private String messageFor(OrderStateChanged event) {
        return event != null ? "Order update: " + event.toStatus() : "";
    }
}
