package com.healthyrolls.technicalservices.persistence.ord.jpa.seed;

import com.healthyrolls.technicalservices.persistence.ord.jpa.menu.MenuItemEntity;
import com.healthyrolls.technicalservices.persistence.ord.jpa.menu.SpringDataMenuItemRepository;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.RestaurantEntity;
import com.healthyrolls.technicalservices.persistence.ord.jpa.restaurant.SpringDataRestaurantRepository;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
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

        for (SeedRestaurant seed : read(args)) {
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
    }

    private SeedRestaurant[] read(ApplicationArguments args) {
        List<String> values = args.getOptionValues("seed");

        if (values == null || values.isEmpty()) {
            try (InputStream input = new ClassPathResource(SEED_FILE).getInputStream()) {
                return new ObjectMapper().readValue(input, SeedRestaurant[].class);
            } catch (JacksonException exception) {
                throw new IllegalArgumentException("Could not deserialize seed file: " + SEED_FILE, exception);
            } catch (Exception exception) {
                throw new IllegalStateException("Could not load restaurant seed file: " + SEED_FILE, exception);
            }
        }

        Path seedFile = Path.of(values.getLast());

        if (!Files.isRegularFile(seedFile)) {
            throw new IllegalArgumentException("Seed file does not exist: " + seedFile);
        }

        try {
            SeedRestaurant[] restaurants = new ObjectMapper().readValue(seedFile.toFile(), SeedRestaurant[].class);
            return restaurants;
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Could not deserialize seed file: " + seedFile, exception);
        }
    }
}
