package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import java.util.Objects;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.dlv.courier.CourierStateChanged;
import com.healthyrolls.domain.dlv.courier.ICourierEventRepository;

@Repository
public class JpaCourierEventRepository implements ICourierEventRepository {

    private final SpringDataCourierEventRepository database;

    public JpaCourierEventRepository(SpringDataCourierEventRepository database) {
        this.database = database;
    }

    @Override
    @Transactional
    public void save(CourierStateChanged event) {
        Objects.requireNonNull(event, "Courier event is required");
        database.save(new CourierEventEntity(event));
    }
}
