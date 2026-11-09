package com.healthyrolls.domain.prc.ledger;

import java.util.List;
import java.util.UUID;

public interface ILedgerRepository {

    void append(LedgerEntry entry);

    List<LedgerEntry> findByOrderId(UUID orderId);

    List<LedgerEntry> findAll();
}
