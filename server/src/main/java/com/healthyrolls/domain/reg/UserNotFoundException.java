package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.UserId;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UserId userId) {
        super("User not found: " + userId.value());
    }
}
