package com.healthyrolls.ui.reg;

import com.healthyrolls.domain.common.UserRole;

public record RegisterRequest(
        String email,
        String hashedPassword,
        String name,
        UserRole role) {

    public RegisterRequest(String email, String hashedPassword, String name) {
        this(email, hashedPassword, name, UserRole.CUSTOMER);
    }
}
