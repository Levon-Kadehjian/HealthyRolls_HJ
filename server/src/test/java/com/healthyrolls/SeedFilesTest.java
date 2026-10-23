package com.healthyrolls;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.seed.SeedUser;
import com.healthyrolls.technicalservices.persistence.ord.jpa.seed.SeedRestaurant;

import tools.jackson.databind.ObjectMapper;

/** Guards the default seed files against drifting from the records they deserialize into. */
class SeedFilesTest {

    private <T> T read(String file, Class<T> type) throws Exception {
        try (InputStream input = new ClassPathResource(file).getInputStream()) {
            return new ObjectMapper().readValue(input, type);
        }
    }

    @Test
    void restaurantSeedFileDeserializes() throws Exception {
        SeedRestaurant[] restaurants = read("seed/restaurants.json", SeedRestaurant[].class);

        assertTrue(restaurants.length > 0, "restaurants.json has no entries");
        assertTrue(
                Stream.of(restaurants).allMatch(r -> r.id() != null && r.menu() != null && !r.menu().isEmpty()),
                "every restaurant needs an id and a non-empty menu");
    }

    @Test
    void userSeedFileCoversEveryRole() throws Exception {
        SeedUser[] users = read("seed/users.json", SeedUser[].class);

        Set<UserRole> roles = Stream.of(users).map(SeedUser::role).collect(Collectors.toSet());
        EnumSet<UserRole> missing = EnumSet.allOf(UserRole.class);
        missing.removeAll(roles);

        assertTrue(missing.isEmpty(), "users.json is missing roles: " + missing);
    }
}
