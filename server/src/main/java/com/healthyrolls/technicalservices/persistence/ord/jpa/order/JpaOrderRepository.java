package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import java.util.Objects;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.technicalservices.persistence.ord.OrderRepository;

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
        OrderEntity entity = database.findById(id.value())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return factory.reconstitute(entity.toRecord());
    }

    @Override
    public Order findNextAccepted() {
        throw new UnsupportedOperationException("Preparation scheduling is not implemented yet");
    }
}
