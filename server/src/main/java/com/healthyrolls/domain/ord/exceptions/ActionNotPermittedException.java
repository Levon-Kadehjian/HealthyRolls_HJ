package com.healthyrolls.domain.ord.exceptions;

public class ActionNotPermittedException extends RuntimeException {

    public ActionNotPermittedException(String message) {
        super(message);
    }
}