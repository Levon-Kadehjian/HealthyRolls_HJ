package com.healthyrolls.domain.ord.ledger;

import java.time.Instant;
import java.util.List;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.domain.ord.pricing.Fare;

public record OrderSummary(
        Order order,
        List<OrderLine> lines,
        Fare fare,
        Instant deliveredAt) {
}
