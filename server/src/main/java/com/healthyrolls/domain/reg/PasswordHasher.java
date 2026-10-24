package com.healthyrolls.domain.reg;

public interface PasswordHasher {
    String hash(String hashedPassword);

    boolean matches(String hashedPassword, String storedHash);
}
