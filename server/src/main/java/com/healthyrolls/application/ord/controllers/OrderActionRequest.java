package com.healthyrolls.application.ord.controllers;

import com.healthyrolls.domain.ord.order.OrderAction;

public record OrderActionRequest(
        OrderAction action,
        String reason) {}
