package com.healthyrolls.application.ord;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.healthyrolls.application.reg.UserContext;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.technicalservices.persistence.ord.OrderEventRepository;
import com.healthyrolls.technicalservices.persistence.ord.OrderRepository;

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
            UserContext actor,
            UUID restaurantId,
            List<OrderLine> items) {

        Objects.requireNonNull(actor, "User context is required");

        // TODO: validate actor has permission to place orders

        UUID customerId = actor.userId();

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

    public void changeOrder(
            OrderId orderId,
            OrderAction action,
            UserContext actor) {
        throw new UnsupportedOperationException();
    }
}
