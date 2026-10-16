package com.healthyrolls.domain.ord.order;

import java.util.UUID;

public record EventId(UUID value) {

    public static EventId parse(String value) {
        return new EventId(UUID.fromString(value));
    }

    public static EventId random() {
        return new EventId(UUID.randomUUID());
    }
}