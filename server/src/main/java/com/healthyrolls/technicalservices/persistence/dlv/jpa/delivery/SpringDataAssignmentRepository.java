package com.healthyrolls.technicalservices.persistence.dlv.jpa.delivery;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAssignmentRepository
        extends JpaRepository<AssignmentEntity, UUID> {

    Optional<AssignmentEntity> findByOrderIdAndActiveTrue(UUID orderId);

    Optional<AssignmentEntity> findByCourierIdAndActiveTrue(UUID courierId);
}