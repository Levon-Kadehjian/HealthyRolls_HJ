package com.healthyrolls.technicalservices.security.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.technicalservices.security.RouteAccessPolicy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends AbstractSessionGateFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

    private final RouteAccessPolicy policy;

    public AuthorizationFilter(RouteAccessPolicy policy) {
        this.policy = policy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (policy.isPublic(req.getMethod(), req.getRequestURI())) {
            chain.doFilter(req, res);
            return;
        }

        AuthenticatedUser user = (AuthenticatedUser) req.getAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE);
        if (user == null) {
            log.warn("Rejected {} {}, no authenticated user on a protected route",
                    req.getMethod(), req.getRequestURI());
            reject(res, HttpStatus.UNAUTHORIZED);
            return;
        }

        if (!policy.isAllowed(user.role(), req.getMethod(), req.getRequestURI())) {
            log.warn("Denied {} {} for user {} with role {}",
                    req.getMethod(), req.getRequestURI(), user.userId().value(), user.role());
            reject(res, HttpStatus.FORBIDDEN);
            return;
        }

        chain.doFilter(req, res);
    }
}
