package com.healthyrolls.application.reg;

import com.healthyrolls.domain.reg.Role;
import java.util.UUID;

public record UserContext(UUID userId, Role role) {
}