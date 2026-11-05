package com.healthyrolls.application.billing;

import java.util.UUID;

import com.healthyrolls.domain.ord.ledger.ILedgerRepository;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;
import com.healthyrolls.domain.ord.ledger.OrderSummary;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.pricing.FareCalculator;

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
        throw new UnsupportedOperationException("Not implemented");
    }

    public LedgerEntry refundOrder(UUID orderId, UUID customerId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
