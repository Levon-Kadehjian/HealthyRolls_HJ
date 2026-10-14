package com.healthyrolls.technicalservices.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public abstract class AbstractSessionGateFilter extends OncePerRequestFilter {

    @Override
    protected abstract void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException;

    protected void reject(HttpServletResponse res, HttpStatus status) throws IOException {
        res.sendError(status.value());
    }
}
