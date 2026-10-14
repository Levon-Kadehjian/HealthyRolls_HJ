package com.healthyrolls.ui.reg;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        Instant expiresAt,
        UserId userId,
        UserRole role) {}
