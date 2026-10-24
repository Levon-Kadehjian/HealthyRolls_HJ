package com.healthyrolls.application.ord;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.List;
import com.healthyrolls.domain.common.UserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.restaurant.IRestaurantRepository;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantAccessPolicy;
import com.healthyrolls.domain.ord.restaurant.RestaurantEventRecorder;
import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final IRestaurantRepository repository;

    private final RestaurantEventRecorder recorder;

    private final RestaurantAccessPolicy accessPolicy;

    public RestaurantService(IRestaurantRepository repository, RestaurantEventRecorder recorder,
            RestaurantAccessPolicy accessPolicy) {
        this.repository = Objects.requireNonNull(repository, "Restaurant repository is required");
        this.recorder = Objects.requireNonNull(recorder, "Restaurant event recorder is required");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "Restaurant access policy is required");
    }

    @Transactional(readOnly = true)
    public List<Restaurant> listRestaurants(AuthenticatedUser actor) {
        Objects.requireNonNull(actor, "Authenticated user is required");
        if (actor.role() == UserRole.COURIER) {
            throw new ActionNotPermittedException("Couriers cannot browse menus");
        }
        if (actor.role() == UserRole.RESTAURANT_STAFF) {
            if (actor.restaurantId() == null) {
                throw new ActionNotPermittedException("No restaurant is linked to this account");
            }
            return List.of(repository.findById(actor.restaurantId().value()));
        }
        return List.copyOf(repository.findAll());
    }

    @Transactional(readOnly = true)
    public Restaurant getForBrowsing(UUID restaurantId, AuthenticatedUser actor) {
        Objects.requireNonNull(actor, "Authenticated user is required");
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        if (actor.role() != UserRole.CUSTOMER && !accessPolicy.canManage(actor, restaurantId)) {
            throw new ActionNotPermittedException("You cannot browse this restaurant's menu");
        }
        return repository.findById(restaurantId);
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

        if (!accessPolicy.canManage(actor, restaurantId)) {
            log.warn("User {} was denied pausing restaurant {}", actor.userId().value(), restaurantId);
            throw new ActionNotPermittedException("You are not allowed to pause this restaurant");
        }

        Restaurant restaurant = repository.findById(restaurantId);

        final RestaurantStatus previousStatus = restaurant.getStatus();
        restaurant.pause();

        repository.save(restaurant);

        recorder.record(new RestaurantStateChanged(
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

        if (!accessPolicy.canReopen(actor)) {
            log.warn("User {} was denied reopening restaurant {}", actor.userId().value(), restaurantId);
            throw new ActionNotPermittedException("You are not allowed to reopen this restaurant");
        }

        Restaurant restaurant = repository.findById(restaurantId);

        final RestaurantStatus previousStatus = restaurant.getStatus();
        restaurant.reopen();

        repository.save(restaurant);

        recorder.record(new RestaurantStateChanged(
                UUID.randomUUID(), restaurantId, previousStatus, restaurant.getStatus(),
                actor.userId().value(), Instant.now()));

    }

}
