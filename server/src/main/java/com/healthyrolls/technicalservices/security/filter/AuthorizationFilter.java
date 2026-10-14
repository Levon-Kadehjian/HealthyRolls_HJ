package com.healthyrolls.technicalservices.security.filter;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.technicalservices.security.RouteAccessPolicy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;

public class AuthorizationFilter extends AbstractSessionGateFilter {

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
            reject(res, HttpStatus.UNAUTHORIZED);
            return;
        }

        if (!policy.isAllowed(user.role(), req.getMethod(), req.getRequestURI())) {
            reject(res, HttpStatus.FORBIDDEN);
            return;
        }

        chain.doFilter(req, res);
    }
}
