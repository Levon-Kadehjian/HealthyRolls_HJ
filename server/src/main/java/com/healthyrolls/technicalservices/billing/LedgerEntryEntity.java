package com.healthyrolls.technicalservices.billing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.ledger.LedgerEntry;
import com.healthyrolls.domain.ord.ledger.LedgerEntryType;
import com.healthyrolls.domain.ord.order.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity {

    @Id
    private UUID entryId;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryType type;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Instant occurredAt;

    protected LedgerEntryEntity() {
    }

    public LedgerEntryEntity(LedgerEntry entry) {
        this.entryId = entry.entryId();
        this.orderId = entry.orderId();
        this.type = entry.type();
        this.customerId = entry.customerId();
        this.amount = entry.amount();
        this.orderStatus = entry.orderStatus();
        this.occurredAt = entry.occurredAt();
    }

    public LedgerEntry toDomain() {
        return new LedgerEntry(entryId, orderId, type, customerId, amount, orderStatus, occurredAt);
    }
}
