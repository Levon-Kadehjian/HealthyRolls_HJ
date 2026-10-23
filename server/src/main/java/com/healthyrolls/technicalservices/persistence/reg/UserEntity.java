package com.healthyrolls.technicalservices.persistence.reg;

import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.User;
import com.healthyrolls.domain.reg.UserRecord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String storedHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private UUID restaurantId;

    @Column(nullable = false)
    private Instant createdAt;

    private Double latitude;

    private Double longitude;

    protected UserEntity() {
    }

    public UserEntity(UUID userId, String email, String storedHash, String name, UserRole role,
            UUID restaurantId, Instant createdAt) {
        this(userId, email, storedHash, name, role, restaurantId, null, null, createdAt);
        }

        public UserEntity(UUID userId, String email, String storedHash, String name, UserRole role,
            UUID restaurantId, Double latitude, Double longitude, Instant createdAt) {
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

    public UserEntity(User user) {
        this(
                user.id().value(),
                user.email(),
                user.storedHash(),
                user.name(),
                user.role(),
                user.restaurantId().map(id -> id.value()).orElse(null),
                user.latitude(),
                user.longitude(),
                user.createdAt());
    }

    public UserEntity(UserRecord record) {
        this(
                record.userId(),
                record.email(),
                record.storedHash(),
                record.name(),
                record.role(),
                record.restaurantId(),
                record.latitude(),
                record.longitude(),
                record.createdAt());
    }

    public UserRecord toRecord() {
        return new UserRecord(
                userId,
                email,
                storedHash,
                name,
                role,
                restaurantId,
                latitude,
                longitude,
                createdAt);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getStoredHash() {
        return storedHash;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
