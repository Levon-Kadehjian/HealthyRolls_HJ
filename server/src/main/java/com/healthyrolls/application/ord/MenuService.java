package com.healthyrolls.application.ord;

import com.healthyrolls.domain.ord.menu.IMenuRepository;
import com.healthyrolls.domain.common.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;
import com.healthyrolls.domain.ord.menu.Menu;
import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;
import com.healthyrolls.domain.ord.menu.MenuItemStateChanged;
import com.healthyrolls.domain.ord.menu.MenuEventRecorder;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;
import java.math.BigDecimal;
import java.time.Instant;
import com.healthyrolls.domain.ord.order.OrderLine;
import com.healthyrolls.domain.ord.restaurant.RestaurantAccessPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final IMenuRepository repository;
    private final RestaurantService restaurantService;
    private final MenuEventRecorder recorder;
    private final RestaurantAccessPolicy accessPolicy;

    public MenuService(IMenuRepository repository, RestaurantService restaurantService, MenuEventRecorder recorder,
            RestaurantAccessPolicy accessPolicy) {
        this.repository = Objects.requireNonNull(repository, "Menu repository is required");
        this.restaurantService = Objects.requireNonNull(restaurantService, "Restaurant service is required");
        this.recorder = Objects.requireNonNull(recorder, "Menu event recorder is required");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "Restaurant access policy is required");
    }

    private void verifyStaff(UUID restaurantId, AuthenticatedUser actor) {
        Objects.requireNonNull(actor, "Authenticated user is required");
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        if (!accessPolicy.canEditMenu(actor, restaurantId)) {
            throw new ActionNotPermittedException("Only staff of this restaurant can edit its menu");
        }
    }

    @Transactional
    public MenuItem addItem(UUID restaurantId, String name, BigDecimal price, AuthenticatedUser actor) {
        verifyStaff(restaurantId, actor);
        restaurantService.getForBrowsing(restaurantId, actor);
        MenuItem item = new MenuItem(UUID.randomUUID(), name, price, MenuItemStatus.AVAILABLE);
        item.edit(name, price);
        return repository.saveItem(restaurantId, item);
    }

    @Transactional
    public MenuItem editItem(UUID restaurantId, UUID itemId, String name, BigDecimal price,
            AuthenticatedUser actor) {
        verifyStaff(restaurantId, actor);
        MenuItem item = repository.findByRestaurantId(restaurantId).findItem(itemId);
        item.edit(name, price);
        return repository.saveItem(restaurantId, item);
    }

    @Transactional
    public MenuItem changeAvailability(UUID restaurantId, UUID itemId, MenuItemStatus status,
            AuthenticatedUser actor) {
        verifyStaff(restaurantId, actor);
        MenuItem item = repository.findByRestaurantId(restaurantId).findItem(itemId);
        final MenuItemStatus previousStatus = item.getStatus();
        item.changeAvailability(status);
        if (previousStatus != item.getStatus()) {
            repository.saveItem(restaurantId, item);
            recorder.record(new MenuItemStateChanged(UUID.randomUUID(), itemId, restaurantId,
                    previousStatus, item.getStatus(), actor.userId().value(), Instant.now()));
        }
        return item;
    }

    @Transactional(readOnly = true)
    public Menu browseMenu(UUID restaurantId, AuthenticatedUser actor) {
        restaurantService.getForBrowsing(restaurantId, actor);
        return repository.findByRestaurantId(restaurantId);
    }

    public List<OrderLine> validateAndResolveItems(UUID restaurantId, List<OrderLine> items) {
        Objects.requireNonNull(restaurantId, "Restaurant ID is required");
        Objects.requireNonNull(items, "Order lines are required");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one line");
        }

        Menu menu = repository.findByRestaurantId(restaurantId);
        if (!restaurantId.equals(menu.getRestaurantId())) {
            throw new IllegalStateException("Repository returned a menu for another restaurant");
        }

        List<OrderLine> validatedItems = new ArrayList<>();

        for (OrderLine line : items) {
            Objects.requireNonNull(line, "Order line is required");

            MenuItem storedItem = menu.findAvailableItem(line.menuItem().getItemId());

            validatedItems.add(new OrderLine(storedItem, line.quantity()));
        }

        return List.copyOf(validatedItems);
    }
}
