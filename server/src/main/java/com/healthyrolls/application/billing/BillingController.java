package com.healthyrolls.application.billing;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.ledger.LedgerEntry;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;
    private final BillingHistoryService historyService;

    public BillingController(BillingService billingService, BillingHistoryService historyService) {
        this.billingService = billingService;
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public List<LedgerEntry> getHistory(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        return historyService.historyFor(user);
    }

    @GetMapping("/customers/{customerId}/history")
    public List<LedgerEntry> getCustomerHistory(
            @PathVariable UUID customerId,
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        if (!user.isOperator() && (!user.isCustomer() || !user.userId().value().equals(customerId))) {
            throw new ActionNotPermittedException("User is not permitted to view this customer history");
        }
        return historyService.historyFor(customerId);
    }

    @GetMapping("/orders/{orderId}")
    public LedgerEntryResponse getInvoice(
            @PathVariable UUID orderId,
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        LedgerEntry entry = historyService.historyFor(user).stream()
                .filter(candidate -> candidate.orderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ActionNotPermittedException("Invoice is not available"));

        return new LedgerEntryResponse(
                entry.entryId(),
                entry.orderId(),
                entry.type(),
                entry.customerId(),
                entry.amount(),
                entry.orderStatus(),
                entry.occurredAt());
    }

    @PostMapping("/orders/{orderId}/refund")
    public LedgerEntryResponse refundOrder(
            @PathVariable UUID orderId,
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {
        LedgerEntry entry = billingService.refundOrder(orderId, user);
        return new LedgerEntryResponse(
                entry.entryId(),
                entry.orderId(),
                entry.type(),
                entry.customerId(),
                entry.amount(),
                entry.orderStatus(),
                entry.occurredAt());
    }
}
