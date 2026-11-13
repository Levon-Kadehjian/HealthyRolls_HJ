package com.healthyrolls.domain.ord.menu;

import java.util.UUID;

public interface IMenuRepository {

    Menu findByRestaurantId(UUID restaurantId);

    MenuItem saveItem(UUID restaurantId, MenuItem item);
}
