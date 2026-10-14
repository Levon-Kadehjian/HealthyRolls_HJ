package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.common.UserRole;

import java.util.Set;
import org.springframework.http.HttpMethod;

public record AccessRule(
        String pathPattern,
        HttpMethod method,
        Set<UserRole> allowedRules,
        boolean isPublic) {
}
