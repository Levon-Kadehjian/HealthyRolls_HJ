package com.healthyrolls.domain.ord;

import java.util.UUID;

public record EventId(UUID value) {

    public static EventId parse(String value) {
        throw new UnsupportedOperationException();
    }

    public static EventId random() {
        throw new UnsupportedOperationException();
    }
}