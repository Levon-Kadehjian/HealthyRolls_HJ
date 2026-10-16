package com.healthyrolls.domain.reg.seed;

import tools.jackson.databind.ObjectMapper;
import com.healthyrolls.application.reg.UserService;
import com.healthyrolls.domain.common.UserRole;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SeedUserLoader {

    private final SeedUserProperties props;
    private final UserService userService;

    public SeedUserLoader(SeedUserProperties props, UserService userService) {
        this.props = props;
        this.userService = userService;
    }

    public void run(String[] args) {
        if (args.length != 2 || !"--seed".equals(args[0])) {
            return;
        }

        load(args[1]);
        verifyEveryRolePresent();
        props.getUsers().forEach(userService::loadSeeded);
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
