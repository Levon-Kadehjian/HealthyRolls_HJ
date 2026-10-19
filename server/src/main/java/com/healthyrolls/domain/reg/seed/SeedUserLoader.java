package com.healthyrolls.domain.reg.seed;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.healthyrolls.application.reg.UserService;
import com.healthyrolls.domain.common.UserRole;

import tools.jackson.databind.ObjectMapper;

@Component
public class SeedUserLoader {

    private static final Logger log = LoggerFactory.getLogger(SeedUserLoader.class);

    private final SeedUserProperties props;
    private final UserService userService;

    public SeedUserLoader(SeedUserProperties props, UserService userService) {
        this.props = props;
        this.userService = userService;
    }

    public void run(String[] args) {
        if (args.length != 2 || !"--seed".equals(args[0])) {
            log.info("No '--seed <file>'' argument given, skipping user seeding");
            return;
        }

        load(args[1]);
        verifyEveryRolePresent();

        long created = 0;
        for (SeedUser seed : props.getUsers()) {
            if (userService.loadSeeded(seed)) {
                created++;
            }
        }
        log.info("Seeded {} of {} users from {}", created, props.getUsers().size(), args[1]);
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
        } catch (Exception exception) {
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
