package com.healthyrolls.technicalservices.persistence.ord.jpa.event;

import com.healthyrolls.domain.ord.order.IOrderEventRepository;
import com.healthyrolls.domain.ord.order.OrderStateChanged;

import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
