package com.healthyrolls.application.dlv;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.application.ord.OrderService;
import com.healthyrolls.domain.dlv.exceptions.InvalidAssignmentTransitionException;
import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStatus;
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
        private final OrderService orderService;

        public CourierService(
                        ICourierRepository repository,
                        DeliveryAccessPolicy policy,
                        DispatchService dispatchService,
                        OrderService orderService,
                        IAssignmentRepository assignmentRepository) {

                this.repository = Objects.requireNonNull(
                                repository, "Courier repository is required");

                this.policy = Objects.requireNonNull(
                                policy, "Delivery access policy is required");

                this.dispatchService = Objects.requireNonNull(
                                dispatchService, "Dispatch service is required");
                this.assignmentRepository = Objects.requireNonNull(
                                assignmentRepository, "Assignment repository is required");
                this.orderService = Objects.requireNonNull(
                                orderService, "Order service is required");
        }

        @Transactional
        public Courier goOnDuty(UserId courierId, AuthenticatedUser actor) {
                Objects.requireNonNull(courierId, "Courier ID is required");
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                if (!policy.isOwnCourier(actor, courierId)) {
                        throw new ActionNotPermittedException(
                                        "You are not allowed to change this courier's duty status");
                }

                Courier courier = repository.findById(courierId)
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

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

                if (!policy.isOwnCourier(actor, courierId)) {
                        throw new ActionNotPermittedException(
                                        "You are not allowed to change this courier's duty status");
                }

                Courier courier = repository.findById(courierId)
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                courier.goOffDuty();
                repository.save(courier);
                return courier;
        }

        @Transactional
        public Order confirmPickup(
                        UserId courierId,
                        OrderId orderId,
                        AuthenticatedUser actor) {

                Objects.requireNonNull(courierId, "Courier ID is required");
                Objects.requireNonNull(orderId, "Order ID is required");
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                if (!policy.isOwnCourier(actor, courierId)) {
                        throw new ActionNotPermittedException(
                                        "You cannot confirm pickup for another courier");
                }

                Courier courier = repository.findAllForDispatch().stream()
                                .filter(candidate -> candidate.getId().equals(courierId))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                if (!policy.canConfirmPickup(actor, courier, orderId)) {
                        throw new ActionNotPermittedException(
                                        "You cannot confirm pickup for this order");
                }

                Assignment assignment = assignmentRepository
                                .findActiveByCourierId(courierId)
                                .filter(current -> current.getOrderId().equals(orderId))
                                .orElseThrow(() -> new InvalidAssignmentTransitionException(
                                                "No active assignment exists for this courier and order"));

                Order order = orderService.getOrder(orderId, actor);

                if (order.status() != OrderStatus.READY) {
                        throw new InvalidTransitionException(
                                        "Only ready orders can be picked up");
                }

                assignment.markPickedUp();
                courier.confirmPickup();

                assignmentRepository.save(assignment);
                repository.save(courier);

                orderService.changeOrder(orderId, OrderAction.PICK_UP, actor);

                return orderService.getOrder(orderId, actor);
        }

        @Transactional
        public Order confirmDelivery(
                        UserId courierId,
                        OrderId orderId,
                        AuthenticatedUser actor) {

                Objects.requireNonNull(courierId, "Courier ID is required");
                Objects.requireNonNull(orderId, "Order ID is required");
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                if (!policy.isOwnCourier(actor, courierId)) {
                        throw new ActionNotPermittedException(
                                        "You cannot confirm delivery for another courier");
                }

                Courier courier = repository.findAllForDispatch().stream()
                                .filter(candidate -> candidate.getId().equals(courierId))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                if (!policy.canConfirmDelivery(actor, courier, orderId)) {
                        throw new ActionNotPermittedException(
                                        "You cannot confirm delivery for this order");
                }

                Assignment assignment = assignmentRepository
                                .findActiveByCourierId(courierId)
                                .filter(current -> current.getOrderId().equals(orderId))
                                .orElseThrow(() -> new InvalidAssignmentTransitionException(
                                                "No active assignment exists for this courier and order"));

                Order order = orderService.getOrder(orderId, actor);

                if (order.status() != OrderStatus.PICKED_UP) {
                        throw new InvalidTransitionException(
                                        "Only picked-up orders can be delivered");
                }

                assignment.markDelivered();
                courier.confirmDelivery();

                assignmentRepository.save(assignment);
                repository.save(courier);

                orderService.changeOrder(orderId, OrderAction.DELIVER, actor);

                dispatchService.onCourierIdle();

                return orderService.getOrder(orderId, actor);
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
