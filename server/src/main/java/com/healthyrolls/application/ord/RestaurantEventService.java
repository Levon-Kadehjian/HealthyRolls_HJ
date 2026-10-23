package com.healthyrolls.application.ord;

import com.healthyrolls.domain.ord.restaurant.IRestaurantEventRepository;
import com.healthyrolls.domain.ord.restaurant.RestaurantEventRecorder;
import com.healthyrolls.domain.ord.restaurant.RestaurantStateChanged;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantEventService implements RestaurantEventRecorder {

    private final IRestaurantEventRepository eventRepository;

    public RestaurantEventService(IRestaurantEventRepository eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "Restaurant event repository is required");
    }

    @Override
    public void record(RestaurantStateChanged event) {
        Objects.requireNonNull(event, "Restaurant event is required");
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<RestaurantStateChanged> historyOf(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        return eventRepository.findByRestaurantId(restaurantId);
    }
}
