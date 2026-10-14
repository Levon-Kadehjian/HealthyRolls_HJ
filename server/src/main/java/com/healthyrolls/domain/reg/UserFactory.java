package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserRole;

public interface UserFactory {

    User create(String email, String storedHash, String name, UserRole role, RestaurantId restaurantId);
}
