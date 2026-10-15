package com.healthyrolls.technicalservices.persistence.ord.jpa.event;

import java.util.Objects;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.order.OrderEventRepository;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

@Repository
public class JpaOrderEventRepository implements OrderEventRepository {

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
}
