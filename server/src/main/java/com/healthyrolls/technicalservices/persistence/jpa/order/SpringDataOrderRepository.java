package com.healthyrolls.technicalservices.persistence.jpa.order;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {
}
