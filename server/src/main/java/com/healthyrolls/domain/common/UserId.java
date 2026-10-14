package com.healthyrolls.domain.common;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "UserId value must not be null");
    }

    public static UserId parse(String s) {
        return new UserId(UUID.fromString(s));
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }
}
