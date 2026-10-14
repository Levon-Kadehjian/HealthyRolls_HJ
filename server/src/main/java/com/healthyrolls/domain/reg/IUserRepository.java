package com.healthyrolls.domain.reg;

import com.healthyrolls.domain.common.UserId;

import java.util.Optional;

public interface IUserRepository {

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
