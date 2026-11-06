package com.healthyrolls.technicalservices.billing;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.ledger.ILedgerRepository;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;

@Repository
public class JpaLedgerRepository implements ILedgerRepository {

    private final SpringDataLedgerRepository springData;

    public JpaLedgerRepository(SpringDataLedgerRepository springData) {
        this.springData = springData;
    }

    @Override
    @Transactional
    public void append(LedgerEntry entry) {
        springData.save(new LedgerEntryEntity(entry));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntry> findByOrderId(UUID orderId) {
        return springData.findByOrderId(orderId).stream().map(LedgerEntryEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LedgerEntry> findAll() {
        return springData.findAll().stream().map(LedgerEntryEntity::toDomain).toList();
    }
}
