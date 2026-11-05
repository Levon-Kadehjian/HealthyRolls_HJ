package com.healthyrolls.application.billing;

import java.util.List;
import java.util.UUID;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;

public class BillingController {

    private final BillingService billingService;
    private final BillingHistoryService historyService;

    public BillingController(BillingService billingService, BillingHistoryService historyService) {
        this.billingService = billingService;
        this.historyService = historyService;
    }

    public List<LedgerEntry> getHistory(AuthenticatedUser user) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<LedgerEntry> getCustomerHistory(UUID customerId, AuthenticatedUser user) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public LedgerEntryResponse getInvoice(UUID orderId, AuthenticatedUser user) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
