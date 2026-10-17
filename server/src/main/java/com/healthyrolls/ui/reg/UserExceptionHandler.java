package com.healthyrolls.ui.reg;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.healthyrolls.domain.reg.EmailAlreadyUsedException;
import com.healthyrolls.domain.reg.InvalidCredentialsException;

import java.util.stream.Collectors;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.healthyrolls.ui.reg")
public class UserExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail onEmailTaken(EmailAlreadyUsedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(CONFLICT, e.getMessage());
        problem.setTitle("Email already registered");
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail onBadCredentials(InvalidCredentialsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, e.getMessage());
        problem.setTitle("Authentication failed");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onInvalidInput(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(BAD_REQUEST, detail);
        problem.setTitle("Invalid request");
        return problem;
    }
}
