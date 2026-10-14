package com.healthyrolls.technicalservices.security.filter;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.reg.TokenVerifier;
import com.healthyrolls.technicalservices.security.RouteAccessPolicy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtException;

public class AuthenticationFilter extends AbstractSessionGateFilter {

    private static final String BEARER = "Bearer ";

    private final TokenVerifier verifier;
    private final RouteAccessPolicy policy;

    public AuthenticationFilter(TokenVerifier verifier, RouteAccessPolicy policy) {
        this.verifier = verifier;
        this.policy = policy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (policy.isPublic(req.getMethod(), req.getRequestURI())) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            reject(res, HttpStatus.UNAUTHORIZED);
            return;
        }

        AuthenticatedUser user;
        try {
            user = verifier.verify(header.substring(BEARER.length()));
        } catch (JwtException e) {
            reject(res, HttpStatus.UNAUTHORIZED);
            return;
        }

        req.setAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE, user);
        chain.doFilter(req, res);
    }
}
