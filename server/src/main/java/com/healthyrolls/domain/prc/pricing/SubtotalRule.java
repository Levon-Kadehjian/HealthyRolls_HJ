package com.healthyrolls.domain.prc.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.healthyrolls.domain.ord.order.OrderLine;

public class SubtotalRule implements PricingRule {

    @Override
    public FareComponent apply(List<OrderLine> lines, BigDecimal subtotal) {
        return new FareComponent("Subtotal", subtotal);
    }
}
