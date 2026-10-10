package com.healthyrolls.domain.order;

import java.util.UUID;

public record OrderId(UUID value) {

    public static OrderId parse(String value) {
        throw new UnsupportedOperationException();
    }

    public static OrderId random() {
        throw new UnsupportedOperationException();
    }
}