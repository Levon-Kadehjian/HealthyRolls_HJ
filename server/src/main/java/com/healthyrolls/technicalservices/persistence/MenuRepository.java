package com.healthyrolls.technicalservices.persistence;

import java.util.UUID;

import com.healthyrolls.domain.ord.menu.Menu;

public interface MenuRepository {

    Menu findByRestaurantId(UUID restaurantId);
}
