package com.healthyrolls.technicalservices.persistence.ord.jpa.event;

import com.healthyrolls.domain.ord.event.IEventRepository;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EventRepository implements IEventRepository {

    private final SpringDataOrderEventRepository database;

    public EventRepository(SpringDataOrderEventRepository database) {
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