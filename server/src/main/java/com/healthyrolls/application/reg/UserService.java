package com.healthyrolls.application.reg;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.AuthToken;
import com.healthyrolls.domain.reg.EmailAlreadyUsedException;
import com.healthyrolls.domain.reg.IUserRepository;
import com.healthyrolls.domain.reg.InvalidCredentialsException;
import com.healthyrolls.domain.reg.TokenIssuer;
import com.healthyrolls.domain.reg.User;
import com.healthyrolls.domain.reg.UserFactory;
import com.healthyrolls.domain.reg.seed.SeedUser;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final UserFactory userFactory;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public UserService(IUserRepository userRepository, UserFactory userFactory, PasswordEncoder passwordEncoder,
            TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.passwordEncoder = passwordEncoder;
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
                passwordEncoder.encode(hashedPassword),
                name,
                effectiveRole,
                null);

        return userRepository.save(user).id();
    }

    public LoginResult login(String email, String hashedPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.passwordMatches(hashedPassword, passwordEncoder)) {
            throw new InvalidCredentialsException();
        }

        AuthToken token = tokenIssuer.issue(user);
        return new LoginResult(token, user.id(), user.role());
    }

    public User findById(UserId id) {
        return userRepository.findById(id).orElseThrow();
    }

    public boolean loadSeeded(String email, String hashedPassword, String name, UserRole role,
            RestaurantId restaurantId) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }

        User user = userFactory.create(
                email,
                passwordEncoder.encode(hashedPassword),
                name,
                role,
                restaurantId);

        userRepository.save(user);
        return true;
    }

    public boolean loadSeeded(SeedUser seed) {
        if (userRepository.existsByEmail(seed.email())) {
            return false;
        }

        userRepository.save(userFactory.createSeeded(seed));
        return true;
    }
}
