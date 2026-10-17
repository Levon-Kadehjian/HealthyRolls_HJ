package com.healthyrolls.ui.ord;

import java.util.UUID;

public record OrderLineResponse(
        UUID menuItemId,
        int quantity) {}
