package com.healthyrolls.application.ord;

import com.healthyrolls.domain.ord.event.IEventRepository;
import com.healthyrolls.domain.ord.order.OrderEventRecorder;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

import java.util.List;
import java.util.Objects;

public class EventService implements OrderEventRecorder {

    private final IEventRepository repository;

    public EventService(IEventRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Event repository is required");
    }

    public void record(OrderStateChanged event) {
        Objects.requireNonNull(event, "Order event is required");
        repository.save(event);
    }

    public List<OrderStateChanged> historyOf(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");
        return repository.findByOrderId(orderId);
    }
}