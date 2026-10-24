package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserRecord(
        UUID userId,
        String email,
        String storedHash,
        String name,
        UserRole role,
        UUID restaurantId,
        Double latitude,
        Double longitude,
        Instant createdAt) {}
