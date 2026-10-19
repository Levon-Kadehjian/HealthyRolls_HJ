package com.healthyrolls.application.ord;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.exceptions.OrderNotFoundException;
import com.healthyrolls.domain.ord.exceptions.RestaurantUnavailableException;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderAccessPolicy;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderEventRecorder;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final IOrderRepository repository;
    private final OrderFactory factory;
    private final OrderEventRecorder recorder;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final OrderAccessPolicy policy;

    public OrderService(
            IOrderRepository repository,
            OrderFactory factory,
            OrderEventRecorder recorder,
            RestaurantService restaurantService,
            MenuService menuService,
            OrderAccessPolicy policy) {

        this.repository = Objects.requireNonNull(repository, "Order repository is required");

        this.factory = Objects.requireNonNull(factory, "Order factory is required");

        this.recorder = Objects.requireNonNull(recorder, "Order event recorder is required");

        this.restaurantService = Objects.requireNonNull(restaurantService, "Restaurant service is required");

        this.menuService = Objects.requireNonNull(menuService, "Menu service is required");

        this.policy = Objects.requireNonNull(policy, "Order access policy is required");
    }

    public OrderId placeOrder(
            UUID restaurantId,
            List<OrderLine> items,
            AuthenticatedUser actor) {

        Objects.requireNonNull(actor, "User context is required");

        if (actor.role() != UserRole.CUSTOMER) {
            throw new ActionNotPermittedException("Only customers can place orders");
        }

        UUID customerId = actor.userId().value();

        if (!restaurantService.isOpen(restaurantId)) {
            throw new RestaurantUnavailableException(restaurantId);
        }

        List<OrderLine> validatedItems = menuService.validateAndResolveItems(restaurantId, items);

        Order order = factory.createNew(
                customerId,
                restaurantId,
                validatedItems);

        OrderId orderId = order.getOrderId();

        repository.save(order);

        recorder.record(new OrderStateChanged(
                EventId.random(),
                orderId,
                OrderStatus.NONE,
                OrderStatus.PLACED,
                customerId,
                Instant.now()));

        log.info("Order {} placed by customer {} at restaurant {}",
                orderId.value(), customerId, restaurantId);

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

        Order order = repository.findByOrderId(orderId);

        if (!policy.canView(actor, order)) {
            throw new OrderNotFoundException(orderId);
        }

        if (!policy.canPerform(actor, order, action)) {
            log.warn("User {} ({}) was denied action {} on order {}",
                    actor.userId().value(), actor.role(), action, orderId.value());
            throw new ActionNotPermittedException("You are not allowed to perform this action on this order");
        }

        boolean cancelledPreparingOrder = action == OrderAction.CANCEL && order.status() == OrderStatus.PREPARING;
        applyAndRecord(order, action, reason, actor);
        if (action == OrderAction.ACCEPT || action == OrderAction.MARK_READY || cancelledPreparingOrder) {
            startNextOrderIfIdle(order.getRestaurantId(), actor);
        }
    }

    @Transactional(readOnly = true)
    public Order getOrder(OrderId orderId, AuthenticatedUser actor) {

        Objects.requireNonNull(orderId, "Order ID is required");
        Objects.requireNonNull(actor, "User context is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        Order o = repository.findByOrderId(orderId);

        if (!policy.canView(actor, o)) {
            throw new OrderNotFoundException(orderId);
        }

        return o;
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

        recorder.record(event);

        log.info("Order {} moved {} -> {} by user {}",
                order.getOrderId().value(), previousStatus, order.status(), actor.userId().value());
    }
}
