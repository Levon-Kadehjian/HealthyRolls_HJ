package com.healthyrolls.domain.reg.seed;

import com.healthyrolls.application.reg.UserService;
import com.healthyrolls.domain.common.UserRole;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class SeedUserLoader implements ApplicationRunner {

    private final SeedUserProperties props;
    private final UserService userService;

    public SeedUserLoader(SeedUserProperties props, UserService userService) {
        this.props = props;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        verifyEveryRolePresent();
        props.getUsers().forEach(userService::loadSeeded);
    }

    public void verifyEveryRolePresent() {
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
