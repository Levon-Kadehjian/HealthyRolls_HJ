package com.healthyrolls.application.prc.controllers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.OrderStatus;
import com.healthyrolls.domain.prc.ledger.LedgerEntryType;

public record LedgerEntryResponse(
                UUID entryId,
                UUID orderId,
                LedgerEntryType type,
                UUID customerId,
                BigDecimal amount,
                OrderStatus orderStatus,
                Instant occurredAt) {}
