package com.healthyrolls.application.reg.controllers;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
                String email,
                String hashedPassword,
                String name,

                @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,

                @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude) {}