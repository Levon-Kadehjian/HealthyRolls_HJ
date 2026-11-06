package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import com.healthyrolls.domain.dlv.courier.CourierStatus;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface SpringDataCourierRepository extends JpaRepository<CourierEntity, UUID> {

    List<CourierEntity> findByStatus(CourierStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourierEntity c order by c.courierId")
    List<CourierEntity> findAllForDispatch();
}
