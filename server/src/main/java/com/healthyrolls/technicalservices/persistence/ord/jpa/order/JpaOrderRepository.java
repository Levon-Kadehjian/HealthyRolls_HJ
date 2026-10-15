package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderRepository;
import com.healthyrolls.domain.ord.order.OrderStatus;

@Repository
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository database;
    private final OrderFactory factory = new OrderFactory();

    public JpaOrderRepository(SpringDataOrderRepository database) {
        this.database = database;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        Objects.requireNonNull(order, "Order is required");
        OrderEntity saved = database.save(new OrderEntity(order));
        return factory.reconstitute(saved.toRecord());
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(OrderId id) {
        Objects.requireNonNull(id, "Order ID is required");

        Optional<OrderEntity> result = database.findById(id.value());

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Order not found");
        }

        OrderEntity entity = result.get();
        return factory.reconstitute(entity.toRecord());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPreparingOrder(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        return database.existsByRestaurantIdAndStatus(restaurantId, OrderStatus.PREPARING);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findNextAccepted(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        Optional<OrderEntity> result = database
                .findFirstByRestaurantIdAndStatusOrderByAcceptedAtAscOrderIdAsc(restaurantId, OrderStatus.ACCEPTED);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        OrderEntity entity = result.get();
        Order order = factory.reconstitute(entity.toRecord()); // Factory stays independent from the jpa layer

        return Optional.of(order);
    }
}
