package com.healthyrolls.application.ord.controllers;

import com.healthyrolls.domain.ord.menu.MenuItemStatus;
import jakarta.validation.constraints.NotNull;

public record MenuAvailabilityRequest(@NotNull MenuItemStatus status) {}
