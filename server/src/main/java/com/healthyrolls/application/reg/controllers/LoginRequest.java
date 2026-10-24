package com.healthyrolls.application.reg.controllers;

public record LoginRequest(
        String email,
        String hashedPassword) {}
