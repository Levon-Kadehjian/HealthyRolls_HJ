package com.healthyrolls.application.ord;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthyrolls.domain.ord.menu.IMenuEventRepository;
import com.healthyrolls.domain.ord.menu.MenuEventRecorder;
import com.healthyrolls.domain.ord.menu.MenuItemStateChanged;

@Service
public class MenuEventService implements MenuEventRecorder {

    private final IMenuEventRepository eventRepository;

    public MenuEventService(IMenuEventRepository eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "Menu event repository is required");
    }

    @Override
    public void record(MenuItemStateChanged event) {
        Objects.requireNonNull(event, "Menu event is required");
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<MenuItemStateChanged> historyOf(UUID restaurantId) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        return eventRepository.findByRestaurantId(restaurantId);
    }
}
