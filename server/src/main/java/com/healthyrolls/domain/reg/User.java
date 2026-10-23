package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;

import java.time.Instant;
import java.util.Optional;

public class User {

    private final UserId userId;
    private final String email;
    private final String storedHash;
    private final String name;
    private final UserRole role;
    private final RestaurantId restaurantId;
    private final Double latitude;
    private final Double longitude;
    private final Instant createdAt;

    public User(UserId userId, String email, String storedHash, String name, UserRole role,
            RestaurantId restaurantId, Instant createdAt) {
        this(userId, email, storedHash, name, role, restaurantId, null, null, createdAt);
    }

    public User(UserId userId, String email, String storedHash, String name, UserRole role,
            RestaurantId restaurantId, Double latitude, Double longitude, Instant createdAt) {
        this.userId = userId;
        this.email = email;
        this.storedHash = storedHash;
        this.name = name;
        this.role = role;
        this.restaurantId = restaurantId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }

    public UserId id() {
        return userId;
    }

    public String email() {
        return email;
    }

    public String storedHash() {
        return storedHash;
    }

    public String name() {
        return name;
    }

    public UserRole role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }

    // empty for roles that are not tied to a restaurant (like RESTAURANT_STAFF)
    public Optional<RestaurantId> restaurantId() {
        return Optional.ofNullable(restaurantId);
    }

    public Double latitude() {
        return latitude;
    }

    public Double longitude() {
        return longitude;
    }

    public boolean worksAt(RestaurantId id) {
        return restaurantId != null && restaurantId.equals(id);
    }

    public boolean passwordMatches(String hashedPassword, PasswordHasher hasher) {
        return hasher.matches(hashedPassword, storedHash);
    }
}
