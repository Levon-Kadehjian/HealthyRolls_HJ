package com.healthyrolls.application.reg;

import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.IUserRepository;
import com.healthyrolls.domain.reg.TokenIssuer;
import com.healthyrolls.domain.reg.User;
import com.healthyrolls.domain.reg.UserFactory;

import org.springframework.security.crypto.password.PasswordEncoder;

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
        throw new UnsupportedOperationException("register not implemented");
    }

    public LoginResult login(String email, String hashedPassword) {
        throw new UnsupportedOperationException("login not implemented");
    }

    public User findById(UserId id) {
        throw new UnsupportedOperationException("findById not implemented");
    }

    public boolean loadSeeded(String email, String hashedPassword, String name, UserRole role,
            RestaurantId restaurantId) {
        throw new UnsupportedOperationException("loadSeeded not implemented");
    }
}
