package com.healthyrolls.application.reg.controllers;

public record RegisterRequest(
        String email,
        String hashedPassword,
        String name) {}
