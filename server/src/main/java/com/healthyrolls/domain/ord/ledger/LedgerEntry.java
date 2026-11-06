package com.healthyrolls.domain.ord.ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.ord.pricing.Fare;

public record LedgerEntry(
        UUID entryId,
        UUID orderId,
        LedgerEntryType type,
        UUID customerId,
        BigDecimal amount,
        OrderStatus orderStatus,
        Instant occurredAt) {

    public static LedgerEntry charge(Order order, Fare fare) {
        return new LedgerEntry(
                UUID.randomUUID(),
                order.getOrderId().value(),
                LedgerEntryType.CHARGE,
                order.getCustomerId(),
                fare.total(),
                order.status(),
                order.getDeliveredAt());
    }

    public static LedgerEntry refund(UUID orderId, BigDecimal amount) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
