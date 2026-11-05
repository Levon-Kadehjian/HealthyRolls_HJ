package com.healthyrolls.application.billing;

import java.util.List;
import java.util.UUID;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.ledger.ILedgerRepository;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;

public class BillingHistoryService {

    private final ILedgerRepository ledgerRepository;

    public BillingHistoryService(ILedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public List<LedgerEntry> historyFor(AuthenticatedUser user) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<LedgerEntry> historyFor(UUID customerId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
