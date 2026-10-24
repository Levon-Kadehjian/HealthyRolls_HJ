package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.AuthenticatedUser;

public interface TokenVerifier {

    AuthenticatedUser verify(String token);
}
