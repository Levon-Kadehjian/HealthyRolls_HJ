package com.healthyrolls.technicalservices.persistence.reg;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.reg.IUserRepository;
import com.healthyrolls.domain.reg.User;
import com.healthyrolls.domain.reg.UserFactory;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaUserRepository implements IUserRepository {

    private final SpringDataUserRepository database;
    private final UserFactory factory = new UserFactory();

    public JpaUserRepository(SpringDataUserRepository database) {
        this.database = database;
    }

    @Override
    @Transactional
    public User save(User user) {
        Objects.requireNonNull(user, "User is required");
        UserEntity saved = database.save(new UserEntity(user));
        return factory.reconstitute(saved.toRecord());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        Objects.requireNonNull(id, "User ID is required");
        return database.findById(id.value())
                .map(UserEntity::toRecord)
                .map(factory::reconstitute);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "Email is required");
        return database.findByEmail(email)
                .map(UserEntity::toRecord)
                .map(factory::reconstitute);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        Objects.requireNonNull(email, "Email is required");
        return database.existsByEmail(email);
    }
}
