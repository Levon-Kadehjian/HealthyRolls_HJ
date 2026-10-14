package com.healthyrolls.ui.reg;

public record RegisterRequest(
        String email,
        String hashedPassword,
        String name) {}
