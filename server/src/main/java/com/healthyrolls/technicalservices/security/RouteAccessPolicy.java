package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.common.UserRole;

import java.util.List;
import java.util.Optional;
import org.springframework.util.AntPathMatcher;

public class RouteAccessPolicy {

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<AccessRule> rules;

    public RouteAccessPolicy(List<AccessRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public boolean isPublic(String method, String path) {
        return firstMatch(method, path).map(AccessRule::isPublic).orElse(false);
    }

    public boolean isAllowed(UserRole role, String method, String path) {
        return firstMatch(method, path)
                .map(rule -> rule.isPublic() || rule.allowedRules().contains(role))
                .orElse(false);
    }

    private Optional<AccessRule> firstMatch(String method, String path) {
        return rules.stream().filter(rule -> matches(rule, method, path)).findFirst();
    }

    private boolean matches(AccessRule rule, String method, String path) {
        return (rule.method() == null || rule.method().matches(method))
                && matcher.match(rule.pathPattern(), path);
    }
}
