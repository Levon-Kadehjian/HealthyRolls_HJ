package com.healthyrolls.application.ntf.controllers;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MarkReadRequest(@NotNull List<@NotNull UUID> notificationIds) {}
