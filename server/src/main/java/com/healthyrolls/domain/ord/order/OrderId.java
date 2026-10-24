package com.healthyrolls.domain.ord.order;

import java.util.Objects;
import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "OrderId value must not be null");
    }

    public static OrderId parse(String value) {
        return new OrderId(UUID.fromString(value));
    }

    public static OrderId fromString(String value) {
        return parse(value);
    }

    public static OrderId random() {
        return new OrderId(UUID.randomUUID());
    }
}
