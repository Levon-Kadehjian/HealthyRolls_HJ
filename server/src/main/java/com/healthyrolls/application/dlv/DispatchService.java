package com.healthyrolls.application.dlv;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.ICourierRepository;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.dlv.delivery.DispatchQueue;
import com.healthyrolls.domain.dlv.delivery.IAssignmentRepository;
import com.healthyrolls.domain.dlv.delivery.NearestCourierSelector;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.ord.restaurant.IRestaurantRepository;
import com.healthyrolls.domain.ord.restaurant.Restaurant;

@Service
public class DispatchService {

    private final ICourierRepository courierRepository;
    private final IAssignmentRepository assignmentRepository;
    private final IOrderRepository orderRepository;
    private final IRestaurantRepository restaurantRepository;
    private final NearestCourierSelector selector;
    private final DispatchQueue queue;

    public DispatchService(
            ICourierRepository courierRepository,
            IAssignmentRepository assignmentRepository,
            IOrderRepository orderRepository,
            IRestaurantRepository restaurantRepository,
            NearestCourierSelector selector,
            DispatchQueue queue) {

        this.courierRepository = Objects.requireNonNull(
                courierRepository, "Courier repository is required");
        this.assignmentRepository = Objects.requireNonNull(
                assignmentRepository, "Assignment repository is required");
        this.orderRepository = Objects.requireNonNull(
                orderRepository, "Order repository is required");
        this.restaurantRepository = Objects.requireNonNull(
                restaurantRepository, "Restaurant repository is required");
        this.selector = Objects.requireNonNull(
                selector, "Courier selector is required");
        this.queue = Objects.requireNonNull(
                queue, "Dispatch queue is required");
    }

    @EventListener
    @Transactional
    public void onOrderStateChanged(OrderStateChanged event) {
        if (event.toStatus() == OrderStatus.READY) {
            dispatch(event.orderId());
        } else if (event.toStatus() == OrderStatus.CANCELLED) {
            queue.remove(event.orderId());
        }
    }

    @Transactional
    public Optional<Assignment> dispatch(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID is required");

        // Lock before checking assignments so competing dispatches see committed offers.
        List<Courier> lockedCouriers = courierRepository.findAllForDispatch();

        Order order = orderRepository.findByOrderId(orderId);

        if (order.status() != OrderStatus.READY) {
            throw new IllegalStateException(
                    "Only ready orders can be dispatched");
        }

        Optional<Assignment> existing = assignmentRepository.findActiveByOrderId(orderId);

        if (existing.isPresent()) {
            queue.remove(orderId);
            return existing;
        }

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId());

        List<Courier> candidates = lockedCouriers.stream()
                .filter(Courier::isAvailable)
                .filter(courier -> assignmentRepository
                        .findActiveByCourierId(courier.getId())
                        .isEmpty())
                .toList();

        Optional<Courier> selected = selector.select(restaurant, candidates);

        if (selected.isEmpty()) {
            queue.enqueue(orderId);
            return Optional.empty();
        }

        Courier courier = selected.get();
        Assignment assignment = new Assignment(orderId, courier.getId());

        assignmentRepository.save(assignment);

        order.assignCourier(courier.getId());
        orderRepository.save(order);

        queue.remove(orderId);

        return Optional.of(assignment);
    }

    @Transactional
    public void onCourierIdle() {
        for (OrderId orderId : queue.snapshot()) {
            Order order = orderRepository.findByOrderId(orderId);

            if (order.status() != OrderStatus.READY) {
                queue.remove(orderId);
                continue;
            }

            if (dispatch(orderId).isEmpty()) {
                break;
            }
        }
    }

    public List<OrderId> queueSnapshot() {
        return queue.snapshot();
    }
}
