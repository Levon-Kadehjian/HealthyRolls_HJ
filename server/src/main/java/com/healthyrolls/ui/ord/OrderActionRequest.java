package com.healthyrolls.ui.ord;

import com.healthyrolls.domain.ord.order.OrderAction;

public record OrderActionRequest(
        OrderAction action,
        String reason) {}
