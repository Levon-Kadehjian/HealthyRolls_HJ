package com.healthyrolls.domain.ord.order;

import java.util.UUID;

public record EventId(UUID value) {

    public static EventId parse(String value) {
        throw new UnsupportedOperationException();
    }

    public static EventId random() {
        throw new UnsupportedOperationException();
    }
}