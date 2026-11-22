package com.healthyrolls.application.dlv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.healthyrolls.application.ord.OrderService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.dlv.DeliveryAccessPolicy;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.CourierStateChanged;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.dlv.courier.ICourierEventRepository;
import com.healthyrolls.domain.dlv.courier.ICourierRepository;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.dlv.delivery.IAssignmentRepository;
import com.healthyrolls.domain.dlv.exceptions.OfferNotFoundException;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStatus;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock
    private ICourierRepository courierRepository;
    @Mock
    private DeliveryAccessPolicy policy;
    @Mock
    private DispatchService dispatchService;
    @Mock
    private OrderService orderService;
    @Mock
    private IAssignmentRepository assignmentRepository;
    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private ICourierEventRepository courierEventRepository;

    private CourierService service;
    private Logger serviceLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        service = new CourierService(
                courierRepository,
                policy,
                dispatchService,
                orderService,
                assignmentRepository,
                orderRepository,
                courierEventRepository);

        serviceLogger = (Logger) LoggerFactory.getLogger(CourierService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        serviceLogger.detachAppender(logAppender);
    }

    // ---------- acceptOffer / declineOffer ----------

    @Test
    void acceptOfferSavesOneCourierEventAfterOrderAssignmentAndCourier() {
        UserId courierId = UserId.random();
        OrderId orderId = OrderId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = idleCourier(courierId);
        Assignment assignment = new Assignment(orderId, courierId);
        Order order = mock(Order.class);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(policy.canAcceptOffer(actor, courier)).thenReturn(true);
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(assignment));
        when(orderRepository.findByOrderId(orderId)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.READY);

        Instant before = Instant.now();
        Order result = service.acceptOffer(courierId, orderId, actor);
        Instant after = Instant.now();

        assertSame(order, result);

        ArgumentCaptor<CourierStateChanged> captor = ArgumentCaptor.forClass(CourierStateChanged.class);
        InOrder inOrder = inOrder(
                orderRepository, assignmentRepository, courierRepository, courierEventRepository);
        inOrder.verify(orderRepository).save(order);
        inOrder.verify(assignmentRepository).save(assignment);
        inOrder.verify(courierRepository).save(courier);
        inOrder.verify(courierEventRepository).save(captor.capture());
        verifyNoMoreInteractions(courierEventRepository);

        CourierStateChanged event = captor.getValue();
        assertEventFields(event, courierId, orderId,
                CourierStatus.IDLE, CourierStatus.ASSIGNED, before, after);

        assertEquals(
                List.of(String.format(
                        "Courier %s accepted order %s, moved IDLE -> ASSIGNED, event %s",
                        courierId.value(), orderId.value(), event.eventId().value())),
                loggedMessages());
    }

    @Test
    void declineOfferSavesOneCourierEventAfterReturningOfferToQueue() {
        UserId courierId = UserId.random();
        OrderId orderId = OrderId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = idleCourier(courierId);
        Assignment assignment = new Assignment(orderId, courierId);

        when(policy.isOwnCourier(actor, courierId)).thenReturn(true);
        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(assignment));

        Instant before = Instant.now();
        Courier result = service.declineOffer(courierId, orderId, actor);
        Instant after = Instant.now();

        assertSame(courier, result);
        assertEquals(CourierStatus.OFFLINE, courier.getStatus());

        ArgumentCaptor<CourierStateChanged> captor = ArgumentCaptor.forClass(CourierStateChanged.class);
        InOrder inOrder = inOrder(courierRepository, dispatchService, courierEventRepository);
        inOrder.verify(courierRepository).save(courier);
        inOrder.verify(dispatchService).returnOfferToQueue(assignment);
        inOrder.verify(courierEventRepository).save(captor.capture());
        verifyNoMoreInteractions(courierEventRepository);

        CourierStateChanged event = captor.getValue();
        // The event carries the declined order even though the courier holds no current order.
        assertEventFields(event, courierId, orderId,
                CourierStatus.IDLE, CourierStatus.OFFLINE, before, after);

        assertEquals(
                List.of(String.format(
                        "Courier %s declined order %s, moved IDLE -> OFFLINE, event %s",
                        courierId.value(), orderId.value(), event.eventId().value())),
                loggedMessages());
    }

    @Test
    void acceptOfferForAnotherCouriersOfferSavesNoEventAndLogsNothing() {
        UserId courierId = UserId.random();
        OrderId orderId = OrderId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = idleCourier(courierId);
        Assignment otherCouriersOffer = new Assignment(orderId, UserId.random());

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(policy.canAcceptOffer(actor, courier)).thenReturn(true);
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(otherCouriersOffer));

        assertThrows(OfferNotFoundException.class,
                () -> service.acceptOffer(courierId, orderId, actor));

        verifyNoInteractions(courierEventRepository);
        assertTrue(loggedMessages().isEmpty());
    }

    @Test
    void declineOfferByAnotherUserSavesNoEventAndLogsNothing() {
        UserId courierId = UserId.random();
        OrderId orderId = OrderId.random();
        AuthenticatedUser actor = courierActor(UserId.random());

        when(policy.isOwnCourier(actor, courierId)).thenReturn(false);

        assertThrows(ActionNotPermittedException.class,
                () -> service.declineOffer(courierId, orderId, actor));

        verifyNoInteractions(courierRepository, courierEventRepository);
        assertTrue(loggedMessages().isEmpty());
    }

    // ---------- other callers of recordCourierTransition() ----------

    @Test
    void goOnDutySavesOneCourierEventThenNotifiesDispatch() {
        UserId courierId = UserId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = courier(courierId, CourierStatus.OFFLINE, null);

        when(policy.isOwnCourier(actor, courierId)).thenReturn(true);
        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));

        Instant before = Instant.now();
        Courier result = service.goOnDuty(courierId, actor);
        Instant after = Instant.now();

        assertSame(courier, result);

        ArgumentCaptor<CourierStateChanged> captor = ArgumentCaptor.forClass(CourierStateChanged.class);
        InOrder inOrder = inOrder(courierRepository, courierEventRepository, dispatchService);
        inOrder.verify(courierRepository).save(courier);
        inOrder.verify(courierEventRepository).save(captor.capture());
        inOrder.verify(dispatchService).onCourierIdle();
        verifyNoMoreInteractions(courierEventRepository);

        // The event uses the courier's current order, which is null here.
        assertEventFields(captor.getValue(), courierId, null,
                CourierStatus.OFFLINE, CourierStatus.IDLE, before, after);
        assertTrue(loggedMessages().isEmpty());
    }

    @Test
    void goOffDutySavesOneCourierEventWithCurrentOrder() {
        UserId courierId = UserId.random();
        OrderId currentOrderId = OrderId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = courier(courierId, CourierStatus.ASSIGNED, currentOrderId);
        Assignment acceptedAssignment = acceptedAssignment(currentOrderId, courierId);

        when(policy.isOwnCourier(actor, courierId)).thenReturn(true);
        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(assignmentRepository.findActiveByCourierId(courierId))
                .thenReturn(Optional.of(acceptedAssignment));

        Instant before = Instant.now();
        Courier result = service.goOffDuty(courierId, actor);
        Instant after = Instant.now();

        assertSame(courier, result);

        ArgumentCaptor<CourierStateChanged> captor = ArgumentCaptor.forClass(CourierStateChanged.class);
        InOrder inOrder = inOrder(courierRepository, courierEventRepository);
        inOrder.verify(courierRepository).save(courier);
        inOrder.verify(courierEventRepository).save(captor.capture());
        verifyNoMoreInteractions(courierEventRepository);
        // An accepted assignment is not returned to the queue.
        verify(dispatchService, never()).returnOfferToQueue(any(Assignment.class));

        assertEventFields(captor.getValue(), courierId, currentOrderId,
                CourierStatus.ASSIGNED, CourierStatus.OFFLINE, before, after);
        assertTrue(loggedMessages().isEmpty());
    }

    @Test
    void confirmPickupSavesOneCourierEventBeforeChangingOrder() {
        UserId courierId = UserId.random();
        OrderId orderId = OrderId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = courier(courierId, CourierStatus.ASSIGNED, orderId);
        Assignment assignment = acceptedAssignment(orderId, courierId);
        Order order = mock(Order.class);

        when(policy.isOwnCourier(actor, courierId)).thenReturn(true);
        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(policy.canConfirmPickup(actor, courier, orderId)).thenReturn(true);
        when(assignmentRepository.findActiveByCourierId(courierId))
                .thenReturn(Optional.of(assignment));
        when(orderService.getOrder(orderId, actor)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.READY);

        Instant before = Instant.now();
        Order result = service.confirmPickup(courierId, orderId, actor);
        Instant after = Instant.now();

        assertSame(order, result);

        ArgumentCaptor<CourierStateChanged> captor = ArgumentCaptor.forClass(CourierStateChanged.class);
        InOrder inOrder = inOrder(
                assignmentRepository, courierRepository, courierEventRepository, orderService);
        inOrder.verify(assignmentRepository).save(assignment);
        inOrder.verify(courierRepository).save(courier);
        inOrder.verify(courierEventRepository).save(captor.capture());
        inOrder.verify(orderService).changeOrder(orderId, OrderAction.PICK_UP, actor);
        verifyNoMoreInteractions(courierEventRepository);

        assertEventFields(captor.getValue(), courierId, orderId,
                CourierStatus.ASSIGNED, CourierStatus.CARRYING, before, after);
        assertTrue(loggedMessages().isEmpty());
    }

    @Test
    void confirmDeliverySavesOneCourierEventBeforeChangingOrderAndNotifyingDispatch() {
        UserId courierId = UserId.random();
        OrderId orderId = OrderId.random();
        AuthenticatedUser actor = courierActor(courierId);
        Courier courier = courier(courierId, CourierStatus.CARRYING, orderId);
        Assignment assignment = pickedUpAssignment(orderId, courierId);
        Order order = mock(Order.class);

        when(policy.isOwnCourier(actor, courierId)).thenReturn(true);
        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(policy.canConfirmDelivery(actor, courier, orderId)).thenReturn(true);
        when(assignmentRepository.findActiveByCourierId(courierId))
                .thenReturn(Optional.of(assignment));
        when(orderService.getOrder(orderId, actor)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.PICKED_UP);

        Instant before = Instant.now();
        Order result = service.confirmDelivery(courierId, orderId, actor);
        Instant after = Instant.now();

        assertSame(order, result);

        ArgumentCaptor<CourierStateChanged> captor = ArgumentCaptor.forClass(CourierStateChanged.class);
        InOrder inOrder = inOrder(
                assignmentRepository, courierRepository, courierEventRepository,
                orderService, dispatchService);
        inOrder.verify(assignmentRepository).save(assignment);
        inOrder.verify(courierRepository).save(courier);
        inOrder.verify(courierEventRepository).save(captor.capture());
        inOrder.verify(orderService).changeOrder(orderId, OrderAction.DELIVER, actor);
        inOrder.verify(dispatchService).onCourierIdle();
        verifyNoMoreInteractions(courierEventRepository);

        // The event carries the delivered order even though the courier has released it.
        assertNull(courier.getCurrentOrderId());
        assertEventFields(captor.getValue(), courierId, orderId,
                CourierStatus.CARRYING, CourierStatus.IDLE, before, after);
        assertTrue(loggedMessages().isEmpty());
    }

    // ---------- helpers ----------

    private static Courier courier(UserId id, CourierStatus status, OrderId currentOrderId) {
        return new Courier(id, "Test Courier", 1.0, 1.0, status, currentOrderId);
    }

    private static Courier idleCourier(UserId id) {
        return courier(id, CourierStatus.IDLE, null);
    }

    private static Assignment acceptedAssignment(OrderId orderId, UserId courierId) {
        Instant now = Instant.now();
        return new Assignment(orderId, courierId, now, now, null, null, true);
    }

    private static Assignment pickedUpAssignment(OrderId orderId, UserId courierId) {
        Instant now = Instant.now();
        return new Assignment(orderId, courierId, now, now, now, null, true);
    }

    private static AuthenticatedUser courierActor(UserId id) {
        return new AuthenticatedUser(id, UserRole.COURIER, null);
    }

    private static void assertEventFields(
            CourierStateChanged event,
            UserId courierId,
            OrderId orderId,
            CourierStatus from,
            CourierStatus to,
            Instant before,
            Instant after) {

        assertNotNull(event.eventId());
        assertEquals(courierId, event.courierId());
        assertEquals(orderId, event.orderId());
        assertEquals(from, event.fromStatus());
        assertEquals(to, event.toStatus());
        assertFalse(event.occurredAt().isBefore(before));
        assertFalse(event.occurredAt().isAfter(after));
    }

    private List<String> loggedMessages() {
        return logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }
}