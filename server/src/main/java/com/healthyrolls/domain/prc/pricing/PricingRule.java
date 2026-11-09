package com.healthyrolls.domain.prc.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.healthyrolls.domain.ord.order.OrderLine;

public interface PricingRule {

    FareComponent apply(List<OrderLine> lines, BigDecimal subtotal);
}
