package com.healthyrolls.technicalservices.persistence.ord.jpa.seed;

import com.healthyrolls.technicalservices.persistence.ord.jpa.menu.MenuItemEntity;
import com.healthyrolls.technicalservices.persistence.ord.jpa.menu.SpringDataMenuItemRepository;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.RestaurantEntity;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.SpringDataRestaurantRepository;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestaurantSeedLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RestaurantSeedLoader.class);
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
        if (!args.containsOption("seed")) {
            log.info("No '--seed' argument given, skipping restaurant seeding");
            return;
        }

        if (restaurants.count() > 0) {
            log.info("Restaurants already present, skipping restaurant seeding");
            return;
        }

        SeedRestaurant[] seeds = read();

        for (SeedRestaurant seed : seeds) {
            RestaurantEntity restaurant = restaurants.save(new RestaurantEntity(
                    seed.id(),
                    seed.name(),
                    seed.status(),
                    seed.latitude(),
                    seed.longitude()));

            for (SeedMenuItem item : seed.menu()) {
                menuItems.save(new MenuItemEntity(
                        item.itemId(),
                        restaurant,
                        item.name(),
                        item.price(),
                        item.status()));
            }
        }

        log.info("Seeded {} restaurants from {}", seeds.length, SEED_FILE);
    }

    private SeedRestaurant[] read() {
        try (InputStream input = new ClassPathResource(SEED_FILE).getInputStream()) {
            return new ObjectMapper().readValue(input, SeedRestaurant[].class);
        } catch (JacksonException exception) {
            log.error("Could not deserialize seed file: {}", SEED_FILE, exception);
            throw new IllegalArgumentException("Could not deserialize seed file: " + SEED_FILE, exception);
        } catch (Exception exception) {
            log.error("Could not load restaurant seed file: {}", SEED_FILE, exception);
            throw new IllegalStateException("Could not load restaurant seed file: " + SEED_FILE, exception);
        }
    }
}
