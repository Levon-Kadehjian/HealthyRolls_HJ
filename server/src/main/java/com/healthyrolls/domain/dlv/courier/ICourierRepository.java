package com.healthyrolls.domain.dlv.courier;

import com.healthyrolls.domain.common.UserId;

import java.util.List;
import java.util.Optional;

public interface ICourierRepository {

    Optional<Courier> findById(UserId id);

    List<Courier> findByStatus(CourierStatus status);

    List<Courier> findAll();

    // Reserves courier rows until the caller's dispatch transaction finishes.
    List<Courier> findAllForDispatch();

    void save(Courier courier);
}
