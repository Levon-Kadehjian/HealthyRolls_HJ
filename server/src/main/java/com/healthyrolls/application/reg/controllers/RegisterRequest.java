package com.healthyrolls.application.reg.controllers;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String hashedPassword,
        @NotBlank String name,

        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,

        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude) {}