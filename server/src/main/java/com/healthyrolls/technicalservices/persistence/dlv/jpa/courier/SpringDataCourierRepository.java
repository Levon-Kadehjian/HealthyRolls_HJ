package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import com.healthyrolls.domain.dlv.courier.CourierStatus;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCourierRepository extends JpaRepository<CourierEntity, UUID> {

    List<CourierEntity> findByStatus(CourierStatus status);
}
