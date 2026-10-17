package com.healthyrolls.ui.ord;

import java.util.UUID;

public record OrderLineRequest(
        UUID menuItemId,
        int quantity) {}
