package com.healthyrolls.application.ord.controllers;

import java.util.UUID;

public record OrderLineRequest(
        UUID menuItemId,
        int quantity) {}
