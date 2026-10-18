package com.healthyrolls.technicalservices;

import com.healthyrolls.domain.ord.order.OrderAccessPolicy;
import com.healthyrolls.domain.ord.order.OrderFactory;
import com.healthyrolls.domain.reg.UserFactory;

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
}
