package com.healthyrolls.technicalservices.billing;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {

    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal smallOrderThreshold;
    private BigDecimal smallOrderSurcharge;
}
