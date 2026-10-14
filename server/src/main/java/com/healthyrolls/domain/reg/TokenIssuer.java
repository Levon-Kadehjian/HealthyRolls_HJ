package com.healthyrolls.domain.reg;

public interface TokenIssuer {

    AuthToken issue(User u);
}
