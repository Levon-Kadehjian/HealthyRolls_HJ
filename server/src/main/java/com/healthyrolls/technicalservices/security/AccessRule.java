package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.common.UserRole;

import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "healthyrolls.security.access")
public record AccessRule(
        String pathPattern,
        HttpMethod method,
        Set<UserRole> allowedRules,
        boolean isPublic) {
}
