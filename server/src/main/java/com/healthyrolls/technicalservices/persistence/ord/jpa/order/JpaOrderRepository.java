package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import com.healthyrolls.domain.ord.exceptions.OrderNotFoundException;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.ord.order.QueueCounts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaOrderRepository implements IOrderRepository {
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
    public Order findByOrderId(OrderId id) {
        Objects.requireNonNull(id, "Order ID is required");

        Optional<OrderEntity> result = database.findById(id.value());

        if (result.isEmpty()) {
            throw new OrderNotFoundException(id);
        }

        OrderEntity entity = result.get();
        return factory.reconstitute(entity.toRecord());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(UUID customerId) {
        Objects.requireNonNull(customerId, "Order ID is required");

        return database.findByCustomerId(customerId).stream().map(entity -> factory.reconstitute(entity.toRecord()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByRestaurantId(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Order ID is required");
        return database.findByRestaurantId(restaurantId).stream().map(entity -> factory.reconstitute(entity.toRecord()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return database.findAll().stream().map(entity -> factory.reconstitute(entity.toRecord()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPreparingOrder(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        return database.existsByRestaurantIdAndStatus(restaurantId, OrderStatus.PREPARING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueCounts> countQueues() {
        Map<UUID, Map<OrderStatus, Long>> byRestaurant = new HashMap<>();
        for (Object[] row : database.countByRestaurantAndStatus(
                List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY))) {
            byRestaurant.computeIfAbsent((UUID) row[0], id -> new HashMap<>())
                    .put((OrderStatus) row[1], (Long) row[2]);
        }
        return byRestaurant.entrySet().stream()
                .map(e -> new QueueCounts(e.getKey(),
                        e.getValue().getOrDefault(OrderStatus.ACCEPTED, 0L),
                        e.getValue().getOrDefault(OrderStatus.PREPARING, 0L),
                        e.getValue().getOrDefault(OrderStatus.READY, 0L)))
                .toList();
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
