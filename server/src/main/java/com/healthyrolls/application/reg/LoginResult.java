package com.healthyrolls.application.reg;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.AuthToken;

public record LoginResult(
        AuthToken token,
        UserId userId,
        UserRole role) {}
