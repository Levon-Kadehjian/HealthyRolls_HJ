package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderLine;

public class FareCalculator {

    private final List<PricingRule> rules;

    public FareCalculator(List<PricingRule> rules) {
        this.rules = rules;
    }

    public Fare compute(Order order) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private BigDecimal subtotal(List<OrderLine> lines) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
