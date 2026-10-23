package com.healthyrolls.application.reg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.AuthToken;
import com.healthyrolls.domain.reg.EmailAlreadyUsedException;
import com.healthyrolls.domain.reg.IUserRepository;
import com.healthyrolls.domain.reg.InvalidCredentialsException;
import com.healthyrolls.domain.reg.PasswordHasher;
import com.healthyrolls.domain.reg.TokenIssuer;
import com.healthyrolls.domain.reg.User;
import com.healthyrolls.domain.reg.UserFactory;
import com.healthyrolls.domain.reg.UserNotFoundException;
import com.healthyrolls.domain.reg.seed.SeedUser;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final IUserRepository userRepository;
    private final UserFactory userFactory;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public UserService(IUserRepository userRepository, UserFactory userFactory, PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    public UserId register(String email, String hashedPassword, String name) {
        return register(email, hashedPassword, name, UserRole.CUSTOMER);
    }

    public UserId register(String email, String hashedPassword, String name, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        UserRole effectiveRole = role != null ? role : UserRole.CUSTOMER;

        User user = userFactory.create(
                email,
                passwordHasher.hash(hashedPassword),
                name,
                effectiveRole,
                null);

        UserId id = userRepository.save(user).id();
        log.info("Registered user {} with role {}", id.value(), effectiveRole);
        return id;
    }

    public LoginResult login(String email, String hashedPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed, no account for {}", email);
                    return new InvalidCredentialsException();
                });

        if (!user.passwordMatches(hashedPassword, passwordHasher)) {
            log.warn("Login failed, wrong password for user {}", user.id().value());
            throw new InvalidCredentialsException();
        }

        AuthToken token = tokenIssuer.issue(user);
        return new LoginResult(token, user.id(), user.role());
    }

    public User findById(UserId id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public boolean loadSeeded(String email, String hashedPassword, String name, UserRole role,
            RestaurantId restaurantId) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }

        User user = userFactory.create(
                email,
                passwordHasher.hash(hashedPassword),
                name,
                role,
                restaurantId);

        userRepository.save(user);
        return true;
    }

    public boolean loadSeeded(SeedUser seed) {
        return loadSeeded(
                seed.email(),
                seed.hashedPassword(),
                seed.name(),
                seed.role(),
                seed.restaurantId());
    }
}
