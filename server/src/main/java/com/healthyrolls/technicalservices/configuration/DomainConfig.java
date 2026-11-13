package com.healthyrolls.technicalservices.configuration;

import com.healthyrolls.domain.dlv.delivery.DispatchQueue;
import com.healthyrolls.domain.dlv.delivery.NearestCourierSelector;
import com.healthyrolls.domain.dlv.DeliveryAccessPolicy;
import com.healthyrolls.domain.ord.order.OrderAccessPolicy;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.ord.restaurant.RestaurantAccessPolicy;
import com.healthyrolls.domain.prc.pricing.DeliveryFeeRule;
import com.healthyrolls.domain.prc.pricing.FareCalculator;
import com.healthyrolls.domain.prc.pricing.PricingRule;
import com.healthyrolls.domain.prc.pricing.ServiceFeeRule;
import com.healthyrolls.domain.prc.pricing.SmallOrderSurchargeRule;
import com.healthyrolls.domain.prc.pricing.SubtotalRule;
import com.healthyrolls.domain.reg.UserFactory;
import com.healthyrolls.technicalservices.persistence.prc.PricingProperties;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public UserFactory userFactory() {
        return new UserFactory();
    }

    @Bean
    public OrderFactory orderFactory() {
        return new OrderFactory();
    }

    @Bean
    public OrderAccessPolicy orderAccessPolicy() {
        return new OrderAccessPolicy();
    }

    @Bean
    public RestaurantAccessPolicy restaurantAccessPolicy() {
        return new RestaurantAccessPolicy();
    }

    @Bean
    public FareCalculator fareCalculator(PricingProperties pricing) {
        List<PricingRule> rules = List.of(
                new SubtotalRule(),
                new DeliveryFeeRule(pricing.getDeliveryFee()),
                new ServiceFeeRule(pricing.getServiceFee()),
                new SmallOrderSurchargeRule(
                        pricing.getSmallOrderThreshold(),
                        pricing.getSmallOrderSurcharge()));
        return new FareCalculator(rules);
    }

    @Bean
    public DeliveryAccessPolicy deliveryAccessPolicy() {
        return new DeliveryAccessPolicy();
    }

    @Bean
    public NearestCourierSelector nearestCourierSelector() {
        return new NearestCourierSelector();
    }

    @Bean
    public DispatchQueue dispatchQueue() {
        return new DispatchQueue();
    }
}
