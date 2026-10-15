package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;

import java.time.Instant;

public class UserFactory {

        public User create(String email, String storedHash, String name, UserRole role,
                        RestaurantId restaurantId) {
                return new User(
                                UserId.random(),
                                email,
                                storedHash,
                                name,
                                role,
                                restaurantId,
                                Instant.now());
        }

        public User reconstitute(UserRecord record) {
                RestaurantId restaurantId = record.restaurantId() == null
                                ? null
                                : new RestaurantId(record.restaurantId());

                return new User(
                                new UserId(record.userId()),
                                record.email(),
                                record.storedHash(),
                                record.name(),
                                record.role(),
                                restaurantId,
                                record.createdAt());
        }

        public User createSeeded(SeedUser seed) {
                throw new UnsupportedOperationException("Not implemented yet");
        }
}
