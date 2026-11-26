package com.healthyrolls.application.ntf.controllers;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;

@RestControllerAdvice(basePackages = "com.healthyrolls.application.ntf.controllers")
public class NotificationExceptionHandler {

    @ExceptionHandler(ActionNotPermittedException.class)
    public ProblemDetail onForbidden(ActionNotPermittedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(FORBIDDEN, e.getMessage());
        problem.setTitle("Action not permitted");
        return problem;
    }
}
