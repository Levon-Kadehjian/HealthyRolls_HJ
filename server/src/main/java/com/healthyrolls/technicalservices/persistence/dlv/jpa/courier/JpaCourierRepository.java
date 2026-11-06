package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.dlv.courier.ICourierRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCourierRepository implements ICourierRepository {

    private final SpringDataCourierRepository springData;

    public JpaCourierRepository(SpringDataCourierRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<Courier> findById(UserId id) {
        return springData.findById(id.value()).map(CourierEntity::toDomain);
    }

    @Override
    public List<Courier> findByStatus(CourierStatus status) {
        return springData.findByStatus(status).stream().map(CourierEntity::toDomain).toList();
    }

    @Override
    public List<Courier> findAll() {
        return springData.findAll().stream().map(CourierEntity::toDomain).toList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<Courier> findAllForDispatch() {
        return springData.findAllForDispatch().stream().map(CourierEntity::toDomain).toList();
    }

    @Override
    public void save(Courier courier) {
        springData.save(CourierEntity.from(courier));
    }
}
