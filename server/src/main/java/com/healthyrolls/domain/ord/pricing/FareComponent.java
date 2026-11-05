package com.healthyrolls.domain.ord.pricing;

import java.math.BigDecimal;

public record FareComponent(String label, BigDecimal amount) {
}
