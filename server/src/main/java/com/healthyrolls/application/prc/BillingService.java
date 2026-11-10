package com.healthyrolls.application.prc;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;
import com.healthyrolls.domain.ord.order.EventId;
import com.healthyrolls.domain.ord.order.IOrderEventRepository;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.OrderAction;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderStateChanged;
import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.prc.ledger.ILedgerRepository;
import com.healthyrolls.domain.prc.ledger.LedgerEntry;
import com.healthyrolls.domain.prc.ledger.LedgerEntryType;
import com.healthyrolls.domain.prc.ledger.OrderSummary;
import com.healthyrolls.domain.prc.pricing.FareCalculator;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final FareCalculator fareCalculator;
    private final IOrderRepository orderRepository;
    private final ILedgerRepository ledgerRepository;
    private final IOrderEventRepository orderEventRepository;

    public BillingService(
            FareCalculator fareCalculator,
            IOrderRepository orderRepository,
            ILedgerRepository ledgerRepository,
            IOrderEventRepository orderEventRepository) {
        this.fareCalculator = fareCalculator;
        this.orderRepository = orderRepository;
        this.ledgerRepository = ledgerRepository;
        this.orderEventRepository = orderEventRepository;
    }

    @EventListener
    public void onOrderStateChanged(OrderStateChanged event) {
        if (event.toStatus() == OrderStatus.DELIVERED) {
            Order order = orderRepository.findByOrderId(event.orderId());
            onDelivered(order);
            log.info("Processed billing on delivery for order {}", event.orderId().value());
        }
    }

    @Transactional
    public OrderSummary onDelivered(Order order) {
        if (order.status() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only delivered orders can be billed");
        }

        Order persistedOrder = orderRepository.findByOrderId(order.getOrderId());
        if (persistedOrder.status() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only delivered orders can be billed");
        }

        final var existingEntries = ledgerRepository.findByOrderId(persistedOrder.getOrderId().value());
        final boolean alreadyCharged = existingEntries.stream()
                .anyMatch(entry -> entry.type() == LedgerEntryType.CHARGE);

        var fare = fareCalculator.compute(persistedOrder);
        if (!alreadyCharged) {
            ledgerRepository.append(LedgerEntry.charge(persistedOrder, fare));
        }

        return new OrderSummary(
                persistedOrder,
                persistedOrder.getItems(),
                fare,
                persistedOrder.getDeliveredAt());
    }

    @Transactional
    public LedgerEntry refundOrder(UUID orderId, AuthenticatedUser actor) {
        if (actor == null || !actor.isOperator()) {
            throw new ActionNotPermittedException("Only operators can refund orders");
        }

        Order order = orderRepository.findByOrderId(new OrderId(orderId));
        return refund(order, actor.userId().value());
    }

    @Transactional
    public LedgerEntry refundOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findByOrderId(new OrderId(orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new ActionNotPermittedException("Order does not belong to this customer");
        }
        return refund(order, customerId);
    }

    private LedgerEntry refund(Order order, UUID actorId) {
        if (order.status() != OrderStatus.DELIVERED) {
            throw new InvalidTransitionException("Only delivered orders can be refunded");
        }

        var entries = ledgerRepository.findByOrderId(order.getOrderId().value());
        final LedgerEntry charge = entries.stream()
                .filter(entry -> entry.type() == LedgerEntryType.CHARGE)
                .findFirst()
                .orElseThrow(() -> new InvalidTransitionException("Order has no original charge"));

        if (entries.stream().anyMatch(entry -> entry.type() == LedgerEntryType.REFUND)) {
            throw new InvalidTransitionException("Order has already been refunded");
        }

        OrderStatus previousStatus = order.status();
        order.apply(OrderAction.REFUND);
        orderRepository.save(order);

        EventId eventId = EventId.random();
        orderEventRepository.save(new OrderStateChanged(
                eventId,
                order.getOrderId(),
                previousStatus,
                order.status(),
                actorId,
                java.time.Instant.now()));

        LedgerEntry refund = LedgerEntry.refund(order, charge.amount().abs());
        ledgerRepository.append(refund);
        log.info("Order {} refunded by user {} with event {}",
                order.getOrderId().value(), actorId, eventId.value());
        return refund;
    }
}
