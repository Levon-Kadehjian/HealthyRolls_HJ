package com.healthyrolls.technicalservices.security;

import java.util.Set;

import org.springframework.http.HttpMethod;

import com.healthyrolls.domain.common.UserRole;

public record AccessRule(
        String pathPattern,
        HttpMethod method,
        Set<UserRole> allowedRules,
        boolean isPublic) {

    public AccessRule {
        allowedRules = allowedRules == null ? Set.of() : Set.copyOf(allowedRules);
    }
}
