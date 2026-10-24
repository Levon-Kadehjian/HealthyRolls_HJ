package com.healthyrolls.technicalservices.configuration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.healthyrolls.application.reg.UserService;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.seed.SeedUser;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class SeedUserLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedUserLoader.class);
    private static final String SEED_FILE = "seed/users.json";

    private final SeedUserProperties props;
    private final UserService userService;

    public SeedUserLoader(SeedUserProperties props, UserService userService) {
        this.props = props;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("seed")) {
            log.info("No '--seed' argument given, skipping user seeding");
            return;
        }

        load();
        verifyEveryRolePresent();

        long created = 0;
        for (SeedUser seed : props.getUsers()) {
            if (userService.loadSeeded(seed)) {
                created++;
            }
        }
        log.info("Seeded {} of {} users from {}", created, props.getUsers().size(), SEED_FILE);
    }

    private void load() {
        try (InputStream input = new ClassPathResource(SEED_FILE).getInputStream()) {
            SeedUser[] users = new ObjectMapper().readValue(input, SeedUser[].class);
            props.setUsers(new ArrayList<>(Arrays.asList(users)));
        } catch (JacksonException exception) {
            log.error("Could not deserialize seed file: {}", SEED_FILE, exception);
            throw new IllegalArgumentException("Could not deserialize seed file: " + SEED_FILE, exception);
        } catch (Exception exception) {
            log.error("Could not load user seed file: {}", SEED_FILE, exception);
            throw new IllegalStateException("Could not load user seed file: " + SEED_FILE, exception);
        }
    }

    private void verifyEveryRolePresent() {
        Set<UserRole> configuredRoles = props.getUsers().stream()
                .map(SeedUser::role)
                .collect(Collectors.toSet());

        EnumSet<UserRole> missingRoles = EnumSet.allOf(UserRole.class);
        missingRoles.removeAll(configuredRoles);

        if (!missingRoles.isEmpty()) {
            log.warn("Missing seeded user roles: {}", missingRoles);
            throw new IllegalStateException("Missing seeded user roles: " + missingRoles);
        }
    }
}
