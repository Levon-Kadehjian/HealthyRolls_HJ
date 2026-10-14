package com.healthyrolls.domain.common;

import java.util.Objects;
import java.util.UUID;

public record RestaurantId(UUID value) {

    public RestaurantId {
        Objects.requireNonNull(value, "RestaurantId value must not be null");
    }

    public static RestaurantId parse(String s) {
        return new RestaurantId(UUID.fromString(s));
    }

    public static RestaurantId random() {
        return new RestaurantId(UUID.randomUUID());
    }
}
