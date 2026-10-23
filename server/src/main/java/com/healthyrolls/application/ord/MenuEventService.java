package com.healthyrolls.application.ord;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import com.healthyrolls.domain.ord.menu.IMenuEventRepository;
import com.healthyrolls.domain.ord.menu.MenuEventRecorder;
import com.healthyrolls.domain.ord.menu.MenuItemStateChanged;
import com.healthyrolls.domain.ord.restaurant.RestaurantAccessPolicy;

@Service
public class MenuEventService implements MenuEventRecorder {

    private final IMenuEventRepository eventRepository;
    private final RestaurantAccessPolicy accessPolicy;

    public MenuEventService(
            IMenuEventRepository eventRepository,
            RestaurantAccessPolicy accessPolicy) {

        this.eventRepository = Objects.requireNonNull(eventRepository, "Menu event repository is required");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "Restaurant access policy is required");
    }

    @Override
    public void record(MenuItemStateChanged event) {
        Objects.requireNonNull(event, "Menu event is required");
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<MenuItemStateChanged> historyOf(AuthenticatedUser actor, UUID restaurantId) {
        Objects.requireNonNull(actor, "Authenticated user is required");
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");

        if (!accessPolicy.canManage(actor, restaurantId)) {
            throw new ActionNotPermittedException("You cannot view this restaurant's menu history");
        }

        return eventRepository.findByRestaurantId(restaurantId);
    }
}
