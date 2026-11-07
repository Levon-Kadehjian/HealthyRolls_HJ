package com.healthyrolls.application.dlv;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.healthyrolls.domain.dlv.courier.CourierStateChanged;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.dlv.courier.ICourierEventRepository;
import com.healthyrolls.domain.dlv.exceptions.OfferNotFoundException;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import java.util.List;
import com.healthyrolls.domain.dlv.exceptions.OrderNotStalledException;

@Service
public class CourierService {

        private static final Logger log = LoggerFactory.getLogger(CourierService.class);

        private final DispatchService dispatchService;
        private final ICourierRepository repository;
        private final DeliveryAccessPolicy policy;
        private final IAssignmentRepository assignmentRepository;
        private final IOrderRepository orderRepository;
        private final OrderService orderService;
        private final ICourierEventRepository courierEventRepository;

        public CourierService(
                        ICourierRepository repository,
                        DeliveryAccessPolicy policy,
                        DispatchService dispatchService,
                        OrderService orderService,
                        IAssignmentRepository assignmentRepository,
                        IOrderRepository orderRepository,
                        ICourierEventRepository courierEventRepository) {

                this.repository = Objects.requireNonNull(
                                repository, "Courier repository is required");

                this.policy = Objects.requireNonNull(
                                policy, "Delivery access policy is required");

                this.dispatchService = Objects.requireNonNull(
                                dispatchService, "Dispatch service is required");
                this.assignmentRepository = Objects.requireNonNull(
                                assignmentRepository, "Assignment repository is required");
                this.orderRepository = Objects.requireNonNull(
                                orderRepository, "Order repository is required");
                this.orderService = Objects.requireNonNull(
                                orderService, "Order service is required");
                this.courierEventRepository = Objects.requireNonNull(
                                courierEventRepository, "Courier event repository is required");
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

                Courier courier = repository.findAllForDispatch().stream()
                                .filter(candidate -> candidate.getId().equals(courierId))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                final CourierStatus previousStatus = courier.getStatus();

                courier.goOnDuty();
                repository.save(courier);
                recordCourierTransition(courier, previousStatus, courier.getCurrentOrderId());

                if (courier.isAvailable()) {
                        dispatchService.onCourierIdle();
                }
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

                Courier courier = repository.findAllForDispatch().stream()
                                .filter(candidate -> candidate.getId().equals(courierId))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                final CourierStatus previousStatus = courier.getStatus();

                courier.goOffDuty();
                repository.save(courier);
                recordCourierTransition(courier, previousStatus, courier.getCurrentOrderId());

                assignmentRepository.findActiveByCourierId(courierId)
                                .filter(assignment -> assignment.getAcceptedAt() == null)
                                .ifPresent(dispatchService::returnOfferToQueue);

                return courier;
        }

        @Transactional
        public Order acceptOffer(UserId courierId, OrderId orderId, AuthenticatedUser actor) {
                Objects.requireNonNull(courierId, "Courier ID is required");
                Objects.requireNonNull(orderId, "Order ID is required");
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                Courier courier = repository.findAllForDispatch().stream()
                                .filter(candidate -> candidate.getId().equals(courierId))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                if (!policy.canAcceptOffer(actor, courier)) {
                        throw new ActionNotPermittedException(
                                        "You are not allowed to accept this offer");
                }

                Assignment assignment = assignmentRepository.findActiveByOrderId(orderId)
                                .orElseThrow(() -> new OfferNotFoundException(orderId));

                if (!assignment.getCourierId().equals(courierId)) {
                        throw new OfferNotFoundException(orderId);
                }

                if (assignment.getAcceptedAt() != null) {
                        throw new OfferNotFoundException(orderId);
                }

                Order order = orderRepository.findByOrderId(orderId);

                if (order.status() != OrderStatus.READY) {
                        throw new InvalidTransitionException(
                                        "Cannot accept an offer for an order in state " + order.status());
                }

                if (order.getCourierId() != null && !order.getCourierId().equals(courierId.value())) {
                        throw new InvalidTransitionException(
                                        "Order " + orderId.value() + " is already assigned to another courier");
                }

                final CourierStatus previousStatus = courier.getStatus();

                order.assignCourier(courierId);
                assignment.accept();
                courier.assign(orderId);

                orderRepository.save(order);
                assignmentRepository.save(assignment);
                repository.save(courier);

                CourierStateChanged event = new CourierStateChanged(
                                EventId.random(),
                                courierId,
                                orderId,
                                previousStatus,
                                courier.getStatus(),
                                Instant.now());

                courierEventRepository.save(event);

                log.info("Courier {} accepted order {}, moved {} -> {}, event {}",
                                courierId.value(), orderId.value(), previousStatus, courier.getStatus(),
                                event.eventId().value());

                return order;
        }

        @Transactional
        public Courier declineOffer(UserId courierId, OrderId orderId, AuthenticatedUser actor) {
                Objects.requireNonNull(courierId, "Courier ID is required");
                Objects.requireNonNull(orderId, "Order ID is required");
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                if (!policy.isOwnCourier(actor, courierId)) {
                        throw new ActionNotPermittedException(
                                        "You are not allowed to decline this offer");
                }

                Courier courier = repository.findAllForDispatch().stream()
                                .filter(candidate -> candidate.getId().equals(courierId))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(courierId));

                Assignment assignment = assignmentRepository.findActiveByOrderId(orderId)
                                .filter(offer -> offer.getCourierId().equals(courierId))
                                .filter(offer -> offer.getAcceptedAt() == null)
                                .orElseThrow(() -> new OfferNotFoundException(orderId));

                final CourierStatus previousStatus = courier.getStatus();

                courier.goOffDuty();
                repository.save(courier);

                dispatchService.returnOfferToQueue(assignment);

                CourierStateChanged event = new CourierStateChanged(
                                EventId.random(),
                                courierId,
                                orderId,
                                previousStatus,
                                courier.getStatus(),
                                Instant.now());

                courierEventRepository.save(event);

                log.info("Courier {} declined order {}, moved {} -> {}, event {}",
                                courierId.value(), orderId.value(), previousStatus, courier.getStatus(),
                                event.eventId().value());

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

                final CourierStatus previousStatus = courier.getStatus();

                assignment.markPickedUp();
                courier.confirmPickup();

                assignmentRepository.save(assignment);
                repository.save(courier);
                recordCourierTransition(courier, previousStatus, orderId);

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

                final CourierStatus previousStatus = courier.getStatus();

                assignment.markDelivered();
                courier.confirmDelivery();

                assignmentRepository.save(assignment);
                repository.save(courier);
                recordCourierTransition(courier, previousStatus, orderId);

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

        @Transactional(readOnly = true)
        public Optional<Order> getActiveOrder(
                        UserId courierId,
                        AuthenticatedUser actor) {

                getCourier(courierId, actor);

                return assignmentRepository.findActiveByCourierId(courierId)
                                .map(assignment -> orderService.getOrder(
                                                assignment.getOrderId(), actor));
        }

        private void recordCourierTransition(
                        Courier courier,
                        CourierStatus previousStatus,
                        OrderId orderId) {

                courierEventRepository.save(new CourierStateChanged(
                                EventId.random(),
                                courier.getId(),
                                orderId,
                                previousStatus,
                                courier.getStatus(),
                                Instant.now()));
        }

        @Transactional(readOnly = true)
        public List<Courier> getStalledCouriers(AuthenticatedUser actor) {
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                if (!policy.canReassignStalledOrder(actor)) {
                        throw new ActionNotPermittedException(
                                        "You are not allowed to view stalled orders");
                }

                return repository.findByStatus(CourierStatus.OFFLINE).stream()
                                .filter(courier -> courier.getCurrentOrderId() != null)
                                .toList();
        }

        @Transactional
        public StalledOrderReassignment reassignStalledOrder(OrderId orderId, AuthenticatedUser actor) {
                Objects.requireNonNull(orderId, "Order ID is required");
                Objects.requireNonNull(actor, "User context is required");
                Objects.requireNonNull(actor.userId(), "User ID is required");

                if (!policy.canReassignStalledOrder(actor)) {
                        throw new ActionNotPermittedException(
                                        "You are not allowed to reassign this order");
                }

                List<Courier> lockedCouriers = repository.findAllForDispatch();

                Order order = orderRepository.findByOrderId(orderId);

                Assignment assignment = assignmentRepository.findActiveByOrderId(orderId)
                                .orElseThrow(() -> new OrderNotStalledException(orderId));

                Courier previousCourier = lockedCouriers.stream()
                                .filter(candidate -> candidate.getId().equals(assignment.getCourierId()))
                                .findFirst()
                                .orElseThrow(() -> new CourierNotFoundException(assignment.getCourierId()));

                boolean stalled = order.status() == OrderStatus.READY
                                && assignment.getAcceptedAt() != null
                                && assignment.getPickedUpAt() == null
                                && previousCourier.getStatus() == CourierStatus.OFFLINE
                                && orderId.equals(previousCourier.getCurrentOrderId());

                if (!stalled) {
                        throw new OrderNotStalledException(orderId);
                }

                final CourierStatus previousStatus = previousCourier.getStatus();

                assignment.cancel();
                order.releaseCourier();
                previousCourier.releaseStalledOrder(orderId);

                assignmentRepository.save(assignment);
                orderRepository.save(order);
                repository.save(previousCourier);

                CourierStateChanged event = new CourierStateChanged(
                                EventId.random(),
                                previousCourier.getId(),
                                orderId,
                                previousStatus,
                                previousCourier.getStatus(),
                                Instant.now());

                courierEventRepository.save(event);

                Courier offeredCourier = dispatchService.dispatch(orderId)
                                .flatMap(offer -> repository.findById(offer.getCourierId()))
                                .orElse(null);

                if (offeredCourier == null) {
                        log.info("Operator {} reassigned order {} from courier {}, no courier available, order queued, event {}",
                                        actor.userId().value(), orderId.value(), previousCourier.getId().value(),
                                        event.eventId().value());
                } else {
                        log.info("Operator {} reassigned order {} from courier {}, offered to courier {}, event {}",
                                        actor.userId().value(), orderId.value(), previousCourier.getId().value(),
                                        offeredCourier.getId().value(), event.eventId().value());
                }

                return new StalledOrderReassignment(orderId, previousCourier, offeredCourier);
        }
}
