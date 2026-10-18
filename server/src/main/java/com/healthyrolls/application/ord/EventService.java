package com.healthyrolls.application.ord;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.event.IEventRepository;
import com.healthyrolls.domain.ord.exceptions.OrderNotFoundException;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderAccessPolicy;
import com.healthyrolls.domain.ord.order.OrderEventRecorder;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService implements OrderEventRecorder {

    private final IEventRepository eventRepository;
    private final IOrderRepository orderRepository;
    private final OrderAccessPolicy accessPolicy;

    public EventService(
            IEventRepository eventRepository,
            IOrderRepository orderRepository,
            OrderAccessPolicy accessPolicy) {

        this.eventRepository = Objects.requireNonNull(eventRepository, "Event repository is required");
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository is required");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "Order access policy is required");
    }

    @Override
    public void record(OrderStateChanged event) {
        Objects.requireNonNull(event, "Order event is required");
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<OrderStateChanged> historyOf(AuthenticatedUser user) {
        Objects.requireNonNull(user, "User context is required");

        return visibleOrders(user).stream()
                .filter(order -> accessPolicy.canView(user, order))
                .map(Order::getOrderId)
                .flatMap(orderId -> eventRepository.findByOrderId(orderId).stream())
                .sorted(Comparator.comparing(OrderStateChanged::occurredAt))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderStateChanged> historyOfOrder(AuthenticatedUser user, OrderId orderId) {
        Objects.requireNonNull(user, "User context is required");
        Objects.requireNonNull(orderId, "Order ID is required");

        Order order = orderRepository.findByOrderId(orderId);

        if (!accessPolicy.canView(user, order)) {
            throw new OrderNotFoundException(orderId);
        }

        return eventRepository.findByOrderId(orderId);
    }

    private List<Order> visibleOrders(AuthenticatedUser user) {
        return switch (user.role()) {
            case CUSTOMER -> orderRepository.findByCustomerId(user.userId().value());
            case RESTAURANT_STAFF -> user.restaurantId() == null
                    ? List.of()
                    : orderRepository.findByRestaurantId(user.restaurantId().value());
            case OPERATOR -> orderRepository.findAll();
            case COURIER -> List.of();
        };
    }
}
