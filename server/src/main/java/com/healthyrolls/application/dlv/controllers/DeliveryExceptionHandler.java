package com.healthyrolls.application.dlv.controllers;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.healthyrolls.domain.dlv.exceptions.CourierCarryingOrderException;
import com.healthyrolls.domain.dlv.exceptions.CourierNotFoundException;
import com.healthyrolls.domain.dlv.exceptions.InvalidCourierTransitionException;
import com.healthyrolls.domain.dlv.exceptions.OfferNotAcceptedException;
import com.healthyrolls.domain.dlv.exceptions.OrderNotStalledException;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.exceptions.InvalidTransitionException;
import com.healthyrolls.domain.ord.exceptions.OrderNotFoundException;

@RestControllerAdvice(basePackages = "com.healthyrolls.application.dlv.controllers")
public class DeliveryExceptionHandler {

    @ExceptionHandler(CourierNotFoundException.class)
    public ProblemDetail onCourierNotFound(CourierNotFoundException e) {
        return problem(NOT_FOUND, "Courier not found", e);
    }

    @ExceptionHandler(CourierCarryingOrderException.class)
    public ProblemDetail onCarrying(CourierCarryingOrderException e) {
        return problem(CONFLICT, "Courier carrying an order", e);
    }

    @ExceptionHandler(InvalidCourierTransitionException.class)
    public ProblemDetail onInvalidCourierTransition(InvalidCourierTransitionException e) {
        return problem(CONFLICT, "Invalid courier transition", e);
    }

    @ExceptionHandler(OfferNotAcceptedException.class)
    public ProblemDetail onOfferNotAccepted(OfferNotAcceptedException e) {
        return problem(CONFLICT, "Offer not accepted", e);
    }

    @ExceptionHandler(OrderNotStalledException.class)
    public ProblemDetail onNotStalled(OrderNotStalledException e) {
        return problem(CONFLICT, "Order not stalled", e);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail onOrderNotFound(OrderNotFoundException e) {
        return problem(NOT_FOUND, "Order not found", e);
    }

    @ExceptionHandler(ActionNotPermittedException.class)
    public ProblemDetail onNotPermitted(ActionNotPermittedException e) {
        return problem(FORBIDDEN, "Action not permitted", e);
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ProblemDetail onInvalidTransition(InvalidTransitionException e) {
        return problem(CONFLICT, "Invalid order transition", e);
    }

    private static ProblemDetail problem(HttpStatus status, String title, RuntimeException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle(title);
        return problem;
    }
}
