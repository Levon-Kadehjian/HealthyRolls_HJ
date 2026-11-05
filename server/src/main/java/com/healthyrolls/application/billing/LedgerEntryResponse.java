package com.healthyrolls.application.billing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.ledger.LedgerEntryType;
import com.healthyrolls.domain.ord.order.OrderStatus;

public record LedgerEntryResponse(
        UUID entryId,
        UUID orderId,
        LedgerEntryType type,
        UUID customerId,
        BigDecimal amount,
        OrderStatus orderStatus,
        Instant occurredAt) {
}
