package com.healthyrolls.domain.ord.order;

import java.util.Objects;
import java.util.UUID;

public record EventId(UUID value) {

    public EventId {
        Objects.requireNonNull(value, "EventId value must not be null");
    }

    public static EventId parse(String value) {
        return new EventId(UUID.fromString(value));
    }

    public static EventId fromString(String value) {
        return parse(value);
    }

    public static EventId random() {
        return new EventId(UUID.randomUUID());
    }
}
