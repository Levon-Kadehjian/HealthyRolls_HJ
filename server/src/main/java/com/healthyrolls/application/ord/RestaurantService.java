package com.healthyrolls.application.ord;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantRepository;

import java.util.Objects;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Restaurant repository is required");
    }

    public boolean isOpen(UUID restaurantId) {
        Objects.requireNonNull(
                restaurantId, "Restaurant ID is required");

        Restaurant restaurant = repository.findById(restaurantId);

        return restaurant.isOpen();
    }

    @Transactional
    public void pauseRestaurant(UUID restaurantId, AuthenticatedUser actor) {

        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        Objects.requireNonNull(actor, "Authenticated user is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        if (actor.role() == UserRole.RESTAURANT_STAFF) {
            if (actor.restaurantId() == null || !actor.restaurantId().value().equals(restaurantId)) {
                throw new SecurityException("You can only pause your own restaurant");
            }
        } else if (actor.role() != UserRole.OPERATOR) {
            throw new SecurityException("Only restaurant staff or operators can pause restaurants");
        }

        Restaurant restaurant = repository.findById(restaurantId);

        restaurant.pause();

        repository.save(restaurant);
    }

    @Transactional
    public void reopenRestaurant(
            UUID restaurantId,
            AuthenticatedUser actor) {

        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        Objects.requireNonNull(actor, "Authenticated user is required");
        Objects.requireNonNull(actor.userId(), "User ID is required");

        if (actor.role() != UserRole.OPERATOR) {
            throw new SecurityException("Only operators can reopen restaurants");
        }

        Restaurant restaurant = repository.findById(restaurantId);

        restaurant.reopen();

        repository.save(restaurant);
    }

}
