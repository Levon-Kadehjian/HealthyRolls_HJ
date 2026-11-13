package com.healthyrolls.application.ord.controllers;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

public record MenuItemRequest(@NotBlank @Size(max = 255) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal price) {}
