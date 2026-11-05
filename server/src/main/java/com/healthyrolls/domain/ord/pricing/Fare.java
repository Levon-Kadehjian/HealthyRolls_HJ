package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;
import java.util.List;

public record Fare(List<FareComponent> components) {

    public BigDecimal total() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
