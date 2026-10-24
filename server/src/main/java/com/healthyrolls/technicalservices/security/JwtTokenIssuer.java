package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.reg.AuthToken;
import com.healthyrolls.domain.reg.TokenIssuer;
import com.healthyrolls.domain.reg.User;

import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private static final String ROLE_CLAIM = "role";
    private static final String RESTAURANT_ID_CLAIM = "restaurantId";

    private final JwtEncoder encoder;
    private final Duration ttl;

    public JwtTokenIssuer(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.ttl = properties.ttl();
    }

    @Override
    public AuthToken issue(User u) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(u.id().value().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(ROLE_CLAIM, u.role().name());
        u.restaurantId().ifPresent(id -> claims.claim(RESTAURANT_ID_CLAIM, id.value().toString()));

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new AuthToken(value, expiresAt);
    }
}
