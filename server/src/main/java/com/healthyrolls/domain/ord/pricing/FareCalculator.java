package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderLine;

public class FareCalculator {

    private final List<PricingRule> rules;

    public FareCalculator(List<PricingRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public Fare compute(Order order) {
        Objects.requireNonNull(order, "Order is required");

        List<OrderLine> lines = order.getItems();
        BigDecimal subtotal = subtotal(lines);
        List<FareComponent> components = rules.stream()
                .map(rule -> rule.apply(lines, subtotal))
                .toList();

        return new Fare(components);
    }

    private BigDecimal subtotal(List<OrderLine> lines) {
        return lines.stream()
                .map(line -> line.menuItem().getPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
