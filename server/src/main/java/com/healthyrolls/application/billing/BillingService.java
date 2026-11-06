package com.healthyrolls.application.billing;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.healthyrolls.domain.ord.ledger.ILedgerRepository;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;
import com.healthyrolls.domain.ord.ledger.OrderSummary;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.ord.pricing.FareCalculator;

@Service
public class BillingService {

    private final FareCalculator fareCalculator;
    private final IOrderRepository orderRepository;
    private final ILedgerRepository ledgerRepository;

    public BillingService(
            FareCalculator fareCalculator,
            IOrderRepository orderRepository,
            ILedgerRepository ledgerRepository) {
        this.fareCalculator = fareCalculator;
        this.orderRepository = orderRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public OrderSummary onDelivered(Order order) {
        if (order.status() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only delivered orders can be billed");
        }

        Order persistedOrder = orderRepository.findByOrderId(order.getOrderId());
        if (persistedOrder.status() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only delivered orders can be billed");
        }

        var fare = fareCalculator.compute(persistedOrder);
        ledgerRepository.append(LedgerEntry.charge(persistedOrder, fare));

        return new OrderSummary(
                persistedOrder,
                persistedOrder.getItems(),
                fare,
                persistedOrder.getDeliveredAt());
    }

    public LedgerEntry refundOrder(UUID orderId, UUID customerId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
