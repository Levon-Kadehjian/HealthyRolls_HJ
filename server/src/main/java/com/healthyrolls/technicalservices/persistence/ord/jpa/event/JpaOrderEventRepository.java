package com.healthyrolls.technicalservices.persistence.ord.jpa.event;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.IOrderEventRepository;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

@Repository
public class JpaOrderEventRepository implements IOrderEventRepository {

    private final SpringDataOrderEventRepository database;

    public JpaOrderEventRepository(SpringDataOrderEventRepository database) {
        this.database = database;
    }

    @Override
    @Transactional
    public void save(OrderStateChanged event) {
        Objects.requireNonNull(event, "Order event is required");
        database.save(new OrderEventEntity(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStateChanged> findByOrderId(OrderId id) {
        Objects.requireNonNull(id, "Order ID is required");

        return database.findByOrderId(id.value()).stream()
                .map(entity -> new OrderStateChanged(
                        new EventId(entity.getEventId()),
                        new OrderId(entity.getOrderId()),
                        entity.getFromStatus(),
                        entity.getToStatus(),
                        entity.getUserId(),
                        entity.getOccurredAt()))
                .sorted(Comparator.comparing(OrderStateChanged::occurredAt))
                .toList();
    }
}
