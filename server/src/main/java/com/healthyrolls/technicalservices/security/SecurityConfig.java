package com.healthyrolls.technicalservices.security;

import com.healthyrolls.technicalservices.security.filter.AuthenticationFilter;
import com.healthyrolls.technicalservices.security.filter.AuthorizationFilter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties p) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(signingKey(p)));
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties p) {
        return NimbusJwtDecoder.withSecretKey(signingKey(p)).build();
    }

    @Bean
    public RouteAccessPolicy accessPolicy(AccessRuleProperties p) {
        return new RouteAccessPolicy(p.rules);
    }

    private SecretKey signingKey(JwtProperties p) {
        return new SecretKeySpec(p.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter(AuthenticationFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilter(AuthorizationFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(2);
        return registration;
    }
}
