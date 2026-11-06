package com.healthyrolls.technicalservices.persistence.dlv.jpa.delivery;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.dlv.delivery.IAssignmentRepository;
import com.healthyrolls.domain.ord.order.OrderId;

@Repository
public class JpaAssignmentRepository implements IAssignmentRepository {

    private final SpringDataAssignmentRepository springData;

    public JpaAssignmentRepository(
            SpringDataAssignmentRepository springData) {
        this.springData = Objects.requireNonNull(
                springData, "Assignment database repository is required");
    }

    @Override
    @Transactional
    public void save(Assignment assignment) {
        Objects.requireNonNull(assignment, "Assignment is required");
        springData.save(AssignmentEntity.from(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Assignment> findActiveByOrderId(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");

        return springData.findByOrderIdAndActiveTrue(orderId.value())
                .map(AssignmentEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Assignment> findActiveByCourierId(UserId courierId) {
        Objects.requireNonNull(courierId, "Courier ID is required");

        return springData.findByCourierIdAndActiveTrue(courierId.value())
                .map(AssignmentEntity::toDomain);
    }
}