package com.healthyrolls.application.ord;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Optional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderEventRepository;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.domain.ord.order.OrderRepository;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;

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
            AuthenticatedUser actor,
            UUID restaurantId,
            List<OrderLine> items) {

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

        Objects.requireNonNull(orderId, "Order ID is required");
        Objects.requireNonNull(action, "Order action is required");
        Objects.requireNonNull(actor, "User context is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        if (actor.role() != UserRole.RESTAURANT_STAFF) {
            throw new SecurityException("Only restaurant staff can accept orders or mark them ready");
        }

        if (action != OrderAction.ACCEPT && action != OrderAction.MARK_READY) {
            throw new UnsupportedOperationException("Only acceptance and marking ready are supported");
        }

        Order order = repository.findById(orderId);

        if (actor.restaurantId() == null || !actor.restaurantId().value().equals(order.getRestaurantId())) {

            throw new SecurityException("You cannot change another restaurant's order");
        }

        applyAndRecord(order, action, actor);
        startNextOrderIfIdle(order.getRestaurantId(), actor);
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
        OrderStatus previousStatus = order.status();

        order.apply(action);

        repository.save(order);

        OrderStateChanged event = new OrderStateChanged(
                EventId.random(),
                order.getOrderId(),
                previousStatus,
                order.status(),
                actor.userId().value(),
                action == OrderAction.ACCEPT ? order.getAcceptedAt() : Instant.now());

        eventRepository.save(event);
    }
}
