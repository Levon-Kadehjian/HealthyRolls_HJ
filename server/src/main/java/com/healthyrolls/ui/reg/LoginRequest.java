package com.healthyrolls.ui.reg;

public record LoginRequest(
        String email,
        String hashedPassword) {}
