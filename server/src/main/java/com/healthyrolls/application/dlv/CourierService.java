package com.healthyrolls.application.dlv;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.dlv.delivery.IAssignmentRepository;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.DeliveryAccessPolicy;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.ICourierRepository;
import com.healthyrolls.domain.dlv.exceptions.CourierNotFoundException;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;

@Service
public class CourierService {

    private final DispatchService dispatchService;
    private final ICourierRepository repository;
    private final DeliveryAccessPolicy policy;
    private final IAssignmentRepository assignmentRepository;

    public CourierService(
            ICourierRepository repository,
            DeliveryAccessPolicy policy,
            DispatchService dispatchService,
            IAssignmentRepository assignmentRepository) {

        this.repository = Objects.requireNonNull(
                repository, "Courier repository is required");

        this.policy = Objects.requireNonNull(
                policy, "Delivery access policy is required");

        this.dispatchService = Objects.requireNonNull(
                dispatchService, "Dispatch service is required");
        this.assignmentRepository = Objects.requireNonNull(
                assignmentRepository, "Assignment repository is required");
    }

    @Transactional
    public Courier goOnDuty(UserId courierId, AuthenticatedUser actor) {
        Objects.requireNonNull(courierId, "Courier ID is required");
        Objects.requireNonNull(actor, "User context is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        Courier courier = repository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        if (!policy.canChangeDuty(actor, courier)) {
            throw new ActionNotPermittedException(
                    "You are not allowed to change this courier's duty status");
        }

        courier.goOnDuty();
        repository.save(courier);
        dispatchService.onCourierIdle();
        return courier;
    }

    @Transactional
    public Courier goOffDuty(UserId courierId, AuthenticatedUser actor) {
        Objects.requireNonNull(courierId, "Courier ID is required");
        Objects.requireNonNull(actor, "User context is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        Courier courier = repository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        if (!policy.canChangeDuty(actor, courier)) {
            throw new ActionNotPermittedException(
                    "You are not allowed to change this courier's duty status");
        }

        courier.goOffDuty();
        repository.save(courier);
        return courier;
    }

    @Transactional(readOnly = true)
    public Courier getCourier(UserId courierId, AuthenticatedUser actor) {
        Objects.requireNonNull(courierId, "Courier ID is required");
        Objects.requireNonNull(actor, "User context is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        Courier courier = repository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));

        if (!policy.canView(actor, courier)) {
            throw new CourierNotFoundException(courierId);
        }

        return courier;
    }

    @Transactional(readOnly = true)
    public Optional<Assignment> getPendingOffer(
            UserId courierId,
            AuthenticatedUser actor) {

        getCourier(courierId, actor);

        return assignmentRepository.findActiveByCourierId(courierId)
                .filter(assignment -> assignment.getAcceptedAt() == null);
    }
}
