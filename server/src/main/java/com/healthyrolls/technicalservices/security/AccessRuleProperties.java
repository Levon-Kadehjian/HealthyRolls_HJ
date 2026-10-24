package com.healthyrolls.technicalservices.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "healthyrolls.security.access")
public record AccessRuleProperties(List<AccessRule> rules) {

    public AccessRuleProperties {
        rules = rules == null ? List.of() : List.copyOf(rules);
    }
}
