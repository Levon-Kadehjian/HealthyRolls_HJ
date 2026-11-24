package com.healthyrolls.application.dlv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.CourierStateChanged;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.dlv.courier.ICourierEventRepository;
import com.healthyrolls.domain.dlv.courier.ICourierRepository;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.dlv.delivery.DispatchQueue;
import com.healthyrolls.domain.dlv.delivery.IAssignmentRepository;
import com.healthyrolls.domain.dlv.delivery.NearestCourierSelector;
import com.healthyrolls.domain.dlv.exceptions.CourierNotFoundException;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.ord.restaurant.IRestaurantRepository;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private ICourierRepository courierRepository;
    @Mock
    private IAssignmentRepository assignmentRepository;
    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IRestaurantRepository restaurantRepository;
    @Mock
    private ICourierEventRepository courierEventRepository;
    @Mock
    private NearestCourierSelector selector;

    private DispatchQueue queue;
    private DispatchService service;

    @BeforeEach
    void setUp() {
        queue = new DispatchQueue();
        service = new DispatchService(
                courierRepository,
                assignmentRepository,
                orderRepository,
                restaurantRepository,
                courierEventRepository,
                selector,
                queue);
    }

    @Test
    void dispatchReturnsExistingActiveAssignmentAndRemovesItFromQueue() {
        OrderId orderId = OrderId.random();
        Order order = mock(Order.class);
        Assignment existing = new Assignment(orderId, UserId.random());
        queue.enqueue(orderId);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of());
        when(orderRepository.findByOrderId(orderId)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.READY);
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(existing));

        Optional<Assignment> result = service.dispatch(orderId);

        assertEquals(Optional.of(existing), result);
        assertTrue(queue.snapshot().isEmpty());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void dispatchQueuesReadyOrderWhenNoCourierIsSelected() {
        OrderId orderId = OrderId.random();
        Order order = mock(Order.class);
        Courier courier = idleCourier();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(
                restaurantId, "Test Restaurant", RestaurantStatus.OPEN, 1.0, 1.0);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(orderRepository.findByOrderId(orderId)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.READY);
        when(order.getRestaurantId()).thenReturn(restaurantId);
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByCourierId(courier.getId()))
                .thenReturn(Optional.empty());
        when(restaurantRepository.findById(restaurantId)).thenReturn(restaurant);
        when(selector.select(restaurant, List.of(courier)))
                .thenReturn(Optional.empty());

        Optional<Assignment> result = service.dispatch(orderId);

        assertTrue(result.isEmpty());
        assertEquals(List.of(orderId), queue.snapshot());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void dispatchAssignsNearestEligibleCourierAndUpdatesOrderAndQueue() {
        OrderId orderId = OrderId.random();
        Order order = mock(Order.class);
        Courier courier = idleCourier();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(
                restaurantId, "Test Restaurant", RestaurantStatus.OPEN, 1.0, 1.0);
        queue.enqueue(orderId);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(orderRepository.findByOrderId(orderId)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.READY);
        when(order.getRestaurantId()).thenReturn(restaurantId);
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByCourierId(courier.getId()))
                .thenReturn(Optional.empty());
        when(restaurantRepository.findById(restaurantId)).thenReturn(restaurant);
        when(selector.select(restaurant, List.of(courier)))
                .thenReturn(Optional.of(courier));
        when(orderRepository.save(order)).thenReturn(order);

        Optional<Assignment> result = service.dispatch(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.orElseThrow().getOrderId());
        assertEquals(courier.getId(), result.orElseThrow().getCourierId());
        verify(assignmentRepository).save(result.orElseThrow());
        verify(order).assignCourier(courier.getId());
        verify(orderRepository).save(order);
        assertTrue(queue.snapshot().isEmpty());
    }

    @Test
    void dispatchRejectsAnOrderThatIsNotReady() {
        OrderId orderId = OrderId.random();
        Order order = mock(Order.class);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of());
        when(orderRepository.findByOrderId(orderId)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.ACCEPTED);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.dispatch(orderId));

        assertEquals("Only ready orders can be dispatched", exception.getMessage());
        verify(assignmentRepository, never()).findActiveByOrderId(orderId);
        verify(restaurantRepository, never()).findById(any(UUID.class));
        assertTrue(queue.snapshot().isEmpty());
    }

    @Test
    void dispatchDoesNotOfferToACourierWithAnActiveAssignment() {
        OrderId orderId = OrderId.random();
        Order order = mock(Order.class);
        Courier courier = idleCourier();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(
                restaurantId, "Test Restaurant", RestaurantStatus.OPEN, 1.0, 1.0);
        Assignment activeAssignment = new Assignment(OrderId.random(), courier.getId());

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(orderRepository.findByOrderId(orderId)).thenReturn(order);
        when(order.status()).thenReturn(OrderStatus.READY);
        when(order.getRestaurantId()).thenReturn(restaurantId);
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.empty());
        when(assignmentRepository.findActiveByCourierId(courier.getId()))
                .thenReturn(Optional.of(activeAssignment));
        when(restaurantRepository.findById(restaurantId)).thenReturn(restaurant);
        when(selector.select(restaurant, List.of()))
                .thenReturn(Optional.empty());

        Optional<Assignment> result = service.dispatch(orderId);

        assertTrue(result.isEmpty());
        assertEquals(List.of(orderId), queue.snapshot());
        verify(selector).select(restaurant, List.of());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void cancellingOrderWithoutAssignmentRemovesItFromQueue() {
        OrderId orderId = OrderId.random();
        queue.enqueue(orderId);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of());
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.empty());

        service.onOrderStateChanged(cancelledEvent(orderId));

        assertTrue(queue.snapshot().isEmpty());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(courierRepository, never()).save(any(Courier.class));
        verify(courierEventRepository, never()).save(any(CourierStateChanged.class));
    }

    @Test
    void cancellingAssignedOrderCancelsAssignmentAndReleasesCourier() {
        OrderId orderId = OrderId.random();
        Courier courier = new Courier(
                UserId.random(),
                "Assigned Courier",
                1.0,
                1.0,
                CourierStatus.ASSIGNED,
                orderId);
        Assignment assignment = new Assignment(orderId, courier.getId());
        queue.enqueue(orderId);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(assignment));

        service.onOrderStateChanged(cancelledEvent(orderId));

        assertTrue(!assignment.isActive());
        assertEquals(CourierStatus.IDLE, courier.getStatus());
        assertEquals(null, courier.getCurrentOrderId());
        assertTrue(queue.snapshot().isEmpty());
        verify(courierRepository).save(courier);
        verify(assignmentRepository).save(assignment);
        verify(courierEventRepository).save(any(CourierStateChanged.class));
    }

    @Test
    void cancellingOrderCancelsAssignmentWhenCourierHasDifferentCurrentOrder() {
        OrderId orderId = OrderId.random();
        Courier courier = new Courier(
                UserId.random(),
                "Assigned Courier",
                1.0,
                1.0,
                CourierStatus.ASSIGNED,
                OrderId.random());
        Assignment assignment = new Assignment(orderId, courier.getId());

        when(courierRepository.findAllForDispatch()).thenReturn(List.of(courier));
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(assignment));

        service.onOrderStateChanged(cancelledEvent(orderId));

        assertTrue(!assignment.isActive());
        assertEquals(CourierStatus.ASSIGNED, courier.getStatus());
        assertTrue(queue.snapshot().isEmpty());
        verify(courierRepository, never()).save(courier);
        verify(assignmentRepository).save(assignment);
        verify(courierEventRepository, never()).save(any(CourierStateChanged.class));
    }

    @Test
    void cancellingOrderThrowsWhenAssignedCourierIsMissing() {
        OrderId orderId = OrderId.random();
        UserId missingCourierId = UserId.random();
        Assignment assignment = new Assignment(orderId, missingCourierId);

        when(courierRepository.findAllForDispatch()).thenReturn(List.of());
        when(assignmentRepository.findActiveByOrderId(orderId))
                .thenReturn(Optional.of(assignment));

        CourierNotFoundException exception = assertThrows(
                CourierNotFoundException.class,
                () -> service.onOrderStateChanged(cancelledEvent(orderId)));

        assertEquals(
                "Courier not found: " + missingCourierId.value(),
                exception.getMessage());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(courierRepository, never()).save(any(Courier.class));
    }

    private OrderStateChanged cancelledEvent(OrderId orderId) {
        return new OrderStateChanged(
                EventId.random(),
                orderId,
                OrderStatus.ACCEPTED,
                OrderStatus.CANCELLED,
                UUID.randomUUID(),
                Instant.now());
    }

    private Courier idleCourier() {
        return new Courier(
                UserId.random(),
                "Test Courier",
                1.0,
                1.0,
                CourierStatus.IDLE,
                null);
    }
}
