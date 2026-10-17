package com.healthyrolls.technicalservices.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "healthyrolls.security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "JWT secret must be 256 bits. We are using HS256") String secret,
        @NotNull Duration ttl) {}
