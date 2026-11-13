package com.healthyrolls.domain.prc.pricing;

import java.math.BigDecimal;

public record FareComponent(String label, BigDecimal amount) {}
