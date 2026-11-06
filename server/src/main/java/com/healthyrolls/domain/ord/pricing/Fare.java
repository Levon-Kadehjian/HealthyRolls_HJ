package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;
import java.util.List;

public record Fare(List<FareComponent> components) {

    public Fare {
        components = List.copyOf(components);
    }

    public BigDecimal total() {
        return components.stream()
                .map(FareComponent::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
