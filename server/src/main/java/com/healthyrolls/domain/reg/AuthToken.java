package com.healthyrolls.domain.reg;

import java.time.Instant;

public record AuthToken(
        String value,
        Instant expiresAt) {}
