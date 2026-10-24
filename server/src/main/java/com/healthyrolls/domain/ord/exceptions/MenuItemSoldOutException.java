package com.healthyrolls.domain.ord.exceptions;

import java.util.UUID;

public class MenuItemSoldOutException extends RuntimeException {

    public MenuItemSoldOutException(UUID menuItemId) {
        super("Menu item is sold out: " + menuItemId);
    }
}