package com.healthyrolls.domain.ord.menu;

import java.util.UUID;

public interface MenuRepository {

    Menu findByRestaurantId(UUID restaurantId);
}
