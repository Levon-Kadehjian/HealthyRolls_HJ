package com.healthyrolls.application.ord.controllers;

import java.util.UUID;

public record OrderLineResponse(
        UUID menuItemId,
        int quantity) {}
