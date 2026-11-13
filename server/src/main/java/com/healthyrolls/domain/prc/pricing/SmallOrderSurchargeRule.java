package com.healthyrolls.domain.prc.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.healthyrolls.domain.ord.order.OrderLine;

public class SmallOrderSurchargeRule implements PricingRule {

    private final BigDecimal threshold;
    private final BigDecimal amount;

    public SmallOrderSurchargeRule(BigDecimal threshold, BigDecimal amount) {
        this.threshold = threshold;
        this.amount = amount;
    }

    @Override
    public FareComponent apply(List<OrderLine> lines, BigDecimal subtotal) {
        BigDecimal surcharge = subtotal.compareTo(threshold) < 0 ? amount : BigDecimal.ZERO;
        return new FareComponent("Small-order surcharge", surcharge);
    }
}
