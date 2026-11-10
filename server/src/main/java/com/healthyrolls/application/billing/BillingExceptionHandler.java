package com.healthyrolls.application.billing;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;
import com.healthyrolls.domain.ord.exceptions.OrderNotFoundException;

@RestControllerAdvice(basePackages = "com.healthyrolls.application.billing")
public class BillingExceptionHandler {

    @ExceptionHandler(ActionNotPermittedException.class)
    public ProblemDetail onForbidden(ActionNotPermittedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(FORBIDDEN, e.getMessage());
        problem.setTitle("Action not permitted");
        return problem;
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ProblemDetail onInvalidTransition(InvalidTransitionException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(CONFLICT, e.getMessage());
        problem.setTitle("Invalid order transition");
        return problem;
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail onOrderNotFound(OrderNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.getMessage());
        problem.setTitle("Order not found");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onInvalidInput(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail onIllegalState(IllegalStateException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(CONFLICT, e.getMessage());
        problem.setTitle("Invalid billing state");
        return problem;
    }
}
