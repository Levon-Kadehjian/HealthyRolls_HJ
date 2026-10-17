package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.reg.PasswordHasher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String hashedPassword) {
        return encoder.encode(hashedPassword);
    }

    @Override
    public boolean matches(String hashedPassword, String storedHash) {
        return encoder.matches(hashedPassword, storedHash);
    }
}
