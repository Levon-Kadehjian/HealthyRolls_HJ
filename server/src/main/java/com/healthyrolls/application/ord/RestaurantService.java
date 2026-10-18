package com.healthyrolls.application.ord;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.restaurant.IRestaurantEventRepository;
import com.healthyrolls.domain.ord.restaurant.IRestaurantRepository;
import com.healthyrolls.domain.ord.restaurant.Restaurant;
import com.healthyrolls.domain.ord.restaurant.RestaurantAccessPolicy;
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

    private final RestaurantAccessPolicy accessPolicy;

    public RestaurantService(IRestaurantRepository repository, IRestaurantEventRepository eventRepository,
            RestaurantAccessPolicy accessPolicy) {
        this.repository = Objects.requireNonNull(repository, "Restaurant repository is required");
        this.eventRepository = Objects.requireNonNull(eventRepository, "Restaurant event repository is required");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "Restaurant access policy is required");
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
            throw new ActionNotPermittedException("You are not allowed to pause this restaurant");
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

        if (!accessPolicy.canManage(actor, restaurantId)) {
            throw new ActionNotPermittedException("You are not allowed to reopen this restaurant");
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
