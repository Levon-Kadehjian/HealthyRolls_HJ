package com.healthyrolls.domain.ord.menu;

import java.util.List;
import java.util.UUID;

public interface IMenuEventRepository {

    void save(MenuItemStateChanged event);

    List<MenuItemStateChanged> findByRestaurantId(UUID restaurantId);
}
