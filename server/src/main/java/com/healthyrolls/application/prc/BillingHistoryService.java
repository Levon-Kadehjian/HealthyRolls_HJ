package com.healthyrolls.application.prc;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.order.IOrderRepository;
import com.healthyrolls.domain.ord.order.OrderAccessPolicy;
import com.healthyrolls.domain.prc.ledger.ILedgerRepository;
import com.healthyrolls.domain.prc.ledger.LedgerEntry;

@Service
public class BillingHistoryService {

    private static final Logger log = LoggerFactory.getLogger(BillingHistoryService.class);

    private final ILedgerRepository ledgerRepository;
    private final IOrderRepository orderRepository;
    private final OrderAccessPolicy accessPolicy;

    public BillingHistoryService(
            ILedgerRepository ledgerRepository,
            IOrderRepository orderRepository,
            OrderAccessPolicy accessPolicy) {
        this.ledgerRepository = ledgerRepository;
        this.orderRepository = orderRepository;
        this.accessPolicy = accessPolicy;
    }

    public List<LedgerEntry> historyFor(AuthenticatedUser user) {
        List<LedgerEntry> history = orderRepository.findAll().stream()
                .filter(order -> accessPolicy.canView(user, order))
                .flatMap(order -> ledgerRepository.findByOrderId(order.getOrderId().value()).stream())
                .map(entry -> user.isCourier()
                        ? new LedgerEntry(
                                entry.entryId(),
                                entry.orderId(),
                                entry.type(),
                                null,
                                null,
                                entry.orderStatus(),
                                entry.occurredAt())
                        : entry)
                .toList();

        log.info("Billing history request completed for user {} with {} entries",
                user.userId().value(), history.size());
        return history;
    }

    public List<LedgerEntry> historyFor(UUID customerId) {
        List<LedgerEntry> history = ledgerRepository.findAll().stream()
                .filter(entry -> customerId.equals(entry.customerId()))
                .toList();

        log.info("Billing history request completed for customer {} with {} entries",
                customerId, history.size());
        return history;
    }
}
