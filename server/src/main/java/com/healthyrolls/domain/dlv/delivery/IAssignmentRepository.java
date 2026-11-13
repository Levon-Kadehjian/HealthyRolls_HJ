package com.healthyrolls.domain.dlv.delivery;

import java.util.Optional;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ord.order.OrderId;

public interface IAssignmentRepository {

    void save(Assignment assignment);

    Optional<Assignment> findActiveByOrderId(OrderId orderId);

    Optional<Assignment> findActiveByCourierId(UserId courierId);
}