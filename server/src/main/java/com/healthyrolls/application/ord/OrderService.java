package com.healthyrolls.application.ord;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderEventRepository;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.domain.ord.order.OrderRepository;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class OrderService {

    private final OrderRepository repository;
    private final OrderFactory factory;
    private final OrderEventRepository eventRepository;
    private final RestaurantService restaurantService;
    private final MenuService menuService;

    public OrderService(
            OrderRepository repository,
            OrderFactory factory,
            OrderEventRepository eventRepository,
            RestaurantService restaurantService,
            MenuService menuService) {

        this.repository = Objects.requireNonNull(repository, "Order repository is required");

        this.factory = Objects.requireNonNull(factory, "Order factory is required");

        this.eventRepository = Objects.requireNonNull(eventRepository, "Order event repository is required");

        this.restaurantService = Objects.requireNonNull(restaurantService, "Restaurant service is required");

        this.menuService = Objects.requireNonNull(menuService, "Menu service is required");
    }

    public OrderId placeOrder(
            UUID restaurantId,
            List<OrderLine> items,
            AuthenticatedUser actor) {

        Objects.requireNonNull(actor, "User context is required");

        if (actor.role() != UserRole.CUSTOMER) {
            throw new SecurityException("Only customers can place orders");
        }

        UUID customerId = actor.userId().value();

        if (!restaurantService.isOpen(restaurantId)) {
            throw new IllegalStateException("Restaurant is not open");
        }

        List<OrderLine> validatedItems = menuService.validateAndResolveItems(restaurantId, items);

        Order order = factory.createNew(
                customerId,
                restaurantId,
                validatedItems);

        OrderId orderId = order.getOrderId();

        repository.save(order);

        return orderId;
    }

    @Transactional
    public void changeOrder(OrderId orderId, OrderAction action, AuthenticatedUser actor) {

        changeOrder(orderId, action, null, actor);
    }

    @Transactional
    public void changeOrder(OrderId orderId, OrderAction action, String reason, AuthenticatedUser actor) {

        Objects.requireNonNull(orderId, "Order ID is required");
        Objects.requireNonNull(action, "Order action is required");
        Objects.requireNonNull(actor, "User context is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        if (action == OrderAction.CANCEL) {
            if (actor.role() != UserRole.CUSTOMER) {
                throw new SecurityException(
                        "Only customer cancellation is currently supported");
            }
        } else {
            if (actor.role() != UserRole.RESTAURANT_STAFF) {
                throw new SecurityException("Only restaurant staff can accept, reject or mark orders ready");
            }

            if (action != OrderAction.ACCEPT && action != OrderAction.MARK_READY && action != OrderAction.REJECT) {
                throw new UnsupportedOperationException("This order action is not supported");
            }
        }

        Order order = repository.findById(orderId);

        if (action == OrderAction.CANCEL) {
            if (!actor.userId().value().equals(order.getCustomerId())) {
                throw new SecurityException(
                        "You cannot cancel another customer's order");
            }
        } else {
            if (actor.restaurantId() == null || !actor.restaurantId().value().equals(order.getRestaurantId())) {
                throw new SecurityException("You cannot change another restaurant's order");
            }
        }

        applyAndRecord(order, action, reason, actor);
        if (action == OrderAction.ACCEPT || action == OrderAction.MARK_READY) {
            startNextOrderIfIdle(order.getRestaurantId(), actor);
        }
    }

    private void startNextOrderIfIdle(UUID restaurantId, AuthenticatedUser actor) {
        if (repository.hasPreparingOrder(restaurantId)) {
            return;
        }

        Optional<Order> nextOrder = repository.findNextAccepted(restaurantId);

        if (nextOrder.isPresent()) {
            Order next = nextOrder.get();

            applyAndRecord(next, OrderAction.START_PREPARING, actor);
        }
    }

    private void applyAndRecord(Order order, OrderAction action, AuthenticatedUser actor) {
        applyAndRecord(order, action, null, actor);
    }

    private void applyAndRecord(Order order, OrderAction action, String reason, AuthenticatedUser actor) {

        final OrderStatus previousStatus = order.status();

        order.apply(action, reason);

        repository.save(order);

        Instant occurredAt;

        if (action == OrderAction.ACCEPT) {
            occurredAt = order.getAcceptedAt();
        } else {
            occurredAt = Instant.now();
        }

        OrderStateChanged event = new OrderStateChanged(
                EventId.random(),
                order.getOrderId(),
                previousStatus,
                order.status(),
                actor.userId().value(),
                occurredAt);

        eventRepository.save(event);
    }
}
