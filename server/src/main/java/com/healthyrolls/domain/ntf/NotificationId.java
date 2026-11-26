package com.healthyrolls.domain.ntf;

import java.util.Objects;
import java.util.UUID;

public record NotificationId(UUID value) {

    public NotificationId {
        Objects.requireNonNull(value, "NotificationId value must not be null");
    }

    public static NotificationId parse(String value) {
        return new NotificationId(UUID.fromString(value));
    }

    public static NotificationId fromString(String value) {
        return parse(value);
    }

    public static NotificationId random() {
        return new NotificationId(UUID.randomUUID());
    }
}
