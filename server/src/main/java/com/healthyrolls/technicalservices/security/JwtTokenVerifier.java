package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.RestaurantId;
import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.common.UserRole;
import com.healthyrolls.domain.reg.TokenVerifier;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenVerifier implements TokenVerifier {

    private static final String ROLE_CLAIM = "role";
    private static final String RESTAURANT_ID_CLAIM = "restaurantId";

    private final JwtDecoder decoder;

    public JwtTokenVerifier(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public AuthenticatedUser verify(String token) {
        Jwt jwt = decoder.decode(token);
        try {
            return new AuthenticatedUser(
                    UserId.parse(jwt.getSubject()),
                    UserRole.valueOf(jwt.getClaimAsString(ROLE_CLAIM)),
                    restaurantId(jwt));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadJwtException("Token claim is missing or malformed", e);
        }
    }

    private RestaurantId restaurantId(Jwt jwt) {
        String value = jwt.getClaimAsString(RESTAURANT_ID_CLAIM);
        return value == null ? null : RestaurantId.parse(value);
    }
}
