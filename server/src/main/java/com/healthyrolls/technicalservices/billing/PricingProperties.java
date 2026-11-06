package com.healthyrolls.technicalservices.billing;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {

    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal smallOrderThreshold;
    private BigDecimal smallOrderSurcharge;

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getSmallOrderThreshold() {
        return smallOrderThreshold;
    }

    public void setSmallOrderThreshold(BigDecimal smallOrderThreshold) {
        this.smallOrderThreshold = smallOrderThreshold;
    }

    public BigDecimal getSmallOrderSurcharge() {
        return smallOrderSurcharge;
    }

    public void setSmallOrderSurcharge(BigDecimal smallOrderSurcharge) {
        this.smallOrderSurcharge = smallOrderSurcharge;
    }
}
