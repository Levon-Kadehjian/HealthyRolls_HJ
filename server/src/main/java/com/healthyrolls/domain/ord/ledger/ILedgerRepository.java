package com.healthyrolls.domain.ord.ledger;

import java.util.List;
import java.util.UUID;

public interface ILedgerRepository {

    void append(LedgerEntry entry);

    List<LedgerEntry> findByOrderId(UUID orderId);

    List<LedgerEntry> findAll();
}
