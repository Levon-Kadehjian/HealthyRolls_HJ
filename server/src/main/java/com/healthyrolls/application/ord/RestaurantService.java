package com.healthyrolls.application.ord;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.IRestaurantEventRepository;
import com.healthyrolls.domain.ord.restaurant.IRestaurantRepository;
import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final IRestaurantRepository repository;

    private final IRestaurantEventRepository eventRepository;

    public RestaurantService(IRestaurantRepository repository, IRestaurantEventRepository eventRepository) {
        this.repository = Objects.requireNonNull(repository, "Restaurant repository is required");
        this.eventRepository = Objects.requireNonNull(eventRepository, "Restaurant event repository is required");
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

        final RestaurantStatus previousStatus = restaurant.getStatus();
        restaurant.pause();

        repository.save(restaurant);

        eventRepository.save(new RestaurantStateChanged(
                UUID.randomUUID(), restaurantId, previousStatus, restaurant.getStatus(),
                actor.userId().value(), Instant.now()));

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

        final RestaurantStatus previousStatus = restaurant.getStatus();
        restaurant.reopen();

        repository.save(restaurant);

        eventRepository.save(new RestaurantStateChanged(
                UUID.randomUUID(), restaurantId, previousStatus, restaurant.getStatus(),
                actor.userId().value(), Instant.now()));

    }

}
