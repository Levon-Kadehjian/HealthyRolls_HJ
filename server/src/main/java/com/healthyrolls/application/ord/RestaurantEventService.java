package com.healthyrolls.application.ord;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.restaurant.IRestaurantEventRepository;
import com.healthyrolls.domain.ord.restaurant.RestaurantAccessPolicy;
import com.healthyrolls.domain.ord.restaurant.RestaurantEventRecorder;
import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;

@Service
public class RestaurantEventService implements RestaurantEventRecorder {

    private final IRestaurantEventRepository eventRepository;
    private final RestaurantAccessPolicy accessPolicy;

    public RestaurantEventService(
            IRestaurantEventRepository eventRepository,
            RestaurantAccessPolicy accessPolicy) {

        this.eventRepository = Objects.requireNonNull(eventRepository, "Restaurant event repository is required");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "Restaurant access policy is required");
    }

    @Override
    public void record(RestaurantStateChanged event) {
        Objects.requireNonNull(event, "Restaurant event is required");
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<RestaurantStateChanged> historyOf(AuthenticatedUser actor, UUID restaurantId) {
        Objects.requireNonNull(actor, "Authenticated user is required");
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        if (!accessPolicy.canManage(actor, restaurantId)) {
            throw new ActionNotPermittedException("You cannot view this restaurant's history");
        }

        return eventRepository.findByRestaurantId(restaurantId);
    }
}
