package com.healthyrolls.technicalservices.persistence.reg;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.reg.IUserRepository;
import com.healthyrolls.domain.reg.User;

import java.util.Optional;

public class UserRepository implements IUserRepository {

    @Override
    public Optional<User> findById(UserId id) {
        throw new UnsupportedOperationException("findById not implemented");
    }

    @Override
    public Optional<User> findByEmail(String email) {
        throw new UnsupportedOperationException("findByEmail not implemented");
    }

    @Override
    public boolean existsByEmail(String email) {
        throw new UnsupportedOperationException("existsByEmail not implemented");
    }

    @Override
    public User save(User user) {
        throw new UnsupportedOperationException("save not implemented");
    }
}
