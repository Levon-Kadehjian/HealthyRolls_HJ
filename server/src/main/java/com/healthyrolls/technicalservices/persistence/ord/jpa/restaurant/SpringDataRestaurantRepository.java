package com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant;

import java.util.UUID;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRestaurantRepository
        extends
            JpaRepository<RestaurantEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RestaurantEntity r where r.restaurantId = :restaurantId")
    Optional<RestaurantEntity> findByIdForUpdate(@Param("restaurantId") UUID restaurantId);
}
