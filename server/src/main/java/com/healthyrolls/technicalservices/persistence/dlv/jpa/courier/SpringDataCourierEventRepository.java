package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCourierEventRepository extends JpaRepository<CourierEventEntity, UUID> {
}
