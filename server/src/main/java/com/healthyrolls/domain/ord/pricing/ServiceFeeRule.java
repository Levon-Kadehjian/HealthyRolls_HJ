package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.healthyrolls.domain.ord.order.OrderLine;

public class ServiceFeeRule implements PricingRule {

    private final BigDecimal fee;

    public ServiceFeeRule(BigDecimal fee) {
        this.fee = fee;
    }

    @Override
    public FareComponent apply(List<OrderLine> lines, BigDecimal subtotal) {
        return new FareComponent("Service fee", fee);
    }
}
