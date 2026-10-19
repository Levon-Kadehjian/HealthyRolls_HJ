package com.healthyrolls.technicalservices.persistence.ord.jpa.seed;

import com.healthyrolls.domain.ord.menu.MenuItemStatus;
import com.healthyrolls.domain.ord.restaurant.RestaurantStatus;
import com.healthyrolls.technicalservices.persistence.ord.jpa.menu.MenuItemEntity;
import com.healthyrolls.technicalservices.persistence.ord.jpa.menu.SpringDataMenuItemRepository;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.RestaurantEntity;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.SpringDataRestaurantRepository;

import java.io.InputStream;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestaurantSeedLoader implements ApplicationRunner {

    private static final String SEED_FILE = "seed/restaurants.json";

    private final SpringDataRestaurantRepository restaurants;
    private final SpringDataMenuItemRepository menuItems;

    public RestaurantSeedLoader(
            SpringDataRestaurantRepository restaurants,
            SpringDataMenuItemRepository menuItems) {

        this.restaurants = restaurants;
        this.menuItems = menuItems;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (restaurants.count() > 0) {
            return;
        }

        for (JsonNode node : read().get("restaurants")) {
            RestaurantEntity restaurant = restaurants.save(new RestaurantEntity(
                    UUID.fromString(node.get("id").asString()),
                    node.get("name").asString(),
                    RestaurantStatus.valueOf(node.get("status").asString()),
                    node.get("latitude").asDouble(),
                    node.get("longitude").asDouble()));

            for (JsonNode item : node.get("menu")) {
                menuItems.save(new MenuItemEntity(
                        UUID.fromString(item.get("itemId").asString()),
                        restaurant,
                        item.get("name").asString(),
                        item.get("price").decimalValue(),
                        MenuItemStatus.valueOf(item.get("status").asString())));
            }
        }
    }

    private JsonNode read() {
        try (InputStream input = new ClassPathResource(SEED_FILE).getInputStream()) {
            return new ObjectMapper().readTree(input);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load restaurant seed file: " + SEED_FILE, exception);
        }
    }
}
