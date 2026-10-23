package com.healthyrolls.application.ord;

import com.healthyrolls.domain.ord.menu.IMenuRepository;
import com.healthyrolls.domain.common.AuthenticatedUser;
import org.springframework.transaction.annotation.Transactional;
import com.healthyrolls.domain.ord.menu.Menu;
import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.domain.ord.order.OrderLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final IMenuRepository repository;
    private final RestaurantService restaurantService;

    public MenuService(IMenuRepository repository, RestaurantService restaurantService) {
        this.repository = Objects.requireNonNull(repository, "Menu repository is required");
        this.restaurantService = Objects.requireNonNull(restaurantService, "Restaurant service is required");
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
