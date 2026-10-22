package com.healthyrolls.application.ord.controllers;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;
import com.healthyrolls.domain.ord.exceptions.MenuItemSoldOutException;
import com.healthyrolls.domain.ord.exceptions.NoAcceptedOrderException;
import com.healthyrolls.domain.ord.exceptions.OrderNotFoundException;
import com.healthyrolls.domain.ord.exceptions.RestaurantNotFoundException;
import com.healthyrolls.domain.ord.exceptions.RestaurantUnavailableException;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.healthyrolls.application.ord.controllers")
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail onNotFound(OrderNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.getMessage());
        problem.setTitle("Order not found");
        return problem;
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ProblemDetail onRestaurantNotFound(RestaurantNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(NOT_FOUND, e.getMessage());
        problem.setTitle("Restaurant not found");
        return problem;
    }

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

    @ExceptionHandler(RestaurantUnavailableException.class)
    public ProblemDetail onUnavailable(RestaurantUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(CONFLICT, e.getMessage());
        problem.setTitle("Restaurant unavailable");
        return problem;
    }

    @ExceptionHandler(MenuItemSoldOutException.class)
    public ProblemDetail onSoldOut(MenuItemSoldOutException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(CONFLICT, e.getMessage());
        problem.setTitle("Menu item sold out");
        return problem;
    }

    @ExceptionHandler(NoAcceptedOrderException.class)
    public ProblemDetail onEmptyQueue(NoAcceptedOrderException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(CONFLICT, e.getMessage());
        problem.setTitle("No accepted order");
        return problem;
    }
}