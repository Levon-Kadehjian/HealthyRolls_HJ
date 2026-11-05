package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.healthyrolls.domain.ord.order.OrderLine;

public class DeliveryFeeRule implements PricingRule {

    private final BigDecimal fee;

    public DeliveryFeeRule(BigDecimal fee) {
        this.fee = fee;
    }

    @Override
    public FareComponent apply(List<OrderLine> lines, BigDecimal subtotal) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
