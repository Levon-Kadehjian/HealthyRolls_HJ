package com.healthyrolls.technicalservices.billing;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.healthyrolls.domain.ord.ledger.ILedgerRepository;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;

@Repository
public class JpaLedgerRepository implements ILedgerRepository {

    private final SpringDataLedgerRepository springData;

    public JpaLedgerRepository(SpringDataLedgerRepository springData) {
        this.springData = springData;
    }

    @Override
    public void append(LedgerEntry entry) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<LedgerEntry> findByOrderId(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<LedgerEntry> findAll() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
