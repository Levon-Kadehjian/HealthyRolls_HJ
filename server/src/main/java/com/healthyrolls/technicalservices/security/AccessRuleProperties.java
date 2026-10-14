package com.healthyrolls.technicalservices.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "healthyrolls.security.access")
public class AccessRuleProperties {

    public List<AccessRule> rules;
}
