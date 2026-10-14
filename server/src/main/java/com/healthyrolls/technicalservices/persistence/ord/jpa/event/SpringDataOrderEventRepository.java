package com.healthyrolls.technicalservices.persistence.ord.jpa.event;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderEventRepository extends JpaRepository<OrderEventEntity, UUID> {
    List<OrderEventEntity> findByOrderId(UUID orderId);
}
