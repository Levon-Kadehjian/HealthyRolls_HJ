package com.healthyrolls.technicalservices.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.healthyrolls.application.reg.UserService;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.seed.SeedUser;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class SeedUserLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedUserLoader.class);

    private final SeedUserProperties props;
    private final UserService userService;

    public SeedUserLoader(SeedUserProperties props, UserService userService) {
        this.props = props;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> values = args.getOptionValues("seed");

        if (values == null || values.isEmpty()) {
            log.info("No '--seed=<file>' argument given, skipping user seeding");
            return;
        }

        String path = values.getLast();

        load(path);
        verifyEveryRolePresent();

        long created = 0;
        for (SeedUser seed : props.getUsers()) {
            if (userService.loadSeeded(seed)) {
                created++;
            }
        }
        log.info("Seeded {} of {} users from {}", created, props.getUsers().size(), path);
    }

    private void load(String path) {
        Path seedFile = Path.of(path);

        if (!Files.isRegularFile(seedFile)) {
            throw new IllegalArgumentException("Seed file does not exist: " + seedFile);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SeedUser[] users = objectMapper.readValue(seedFile.toFile(), SeedUser[].class);
            props.setUsers(new ArrayList<>(Arrays.asList(users)));
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Could not deserialize seed file: " + seedFile, exception);
        }
    }

    private void verifyEveryRolePresent() {
        Set<UserRole> configuredRoles = props.getUsers().stream()
                .map(SeedUser::role)
                .collect(Collectors.toSet());

        EnumSet<UserRole> missingRoles = EnumSet.allOf(UserRole.class);
        missingRoles.removeAll(configuredRoles);

        if (!missingRoles.isEmpty()) {
            throw new IllegalStateException("Missing seeded user roles: " + missingRoles);
        }
    }
}
