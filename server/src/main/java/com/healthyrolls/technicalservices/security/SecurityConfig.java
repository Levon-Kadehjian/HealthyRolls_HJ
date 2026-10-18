package com.healthyrolls.technicalservices.security;

import com.healthyrolls.domain.reg.TokenVerifier;
import com.healthyrolls.technicalservices.security.filter.AuthenticationFilter;
import com.healthyrolls.technicalservices.security.filter.AuthorizationFilter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtEncoder jwtEncoder(JwtProperties p) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey(p)));
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties p) {
        return NimbusJwtDecoder.withSecretKey(signingKey(p)).build();
    }

    @Bean
    public RouteAccessPolicy accessPolicy(AccessRuleProperties p) {
        return new RouteAccessPolicy(p.rules());
    }

    private SecretKey signingKey(JwtProperties p) {
        return new SecretKeySpec(p.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // Bear with me:
    // Here we allow all for the requests to hit our own implemtation of the chain. Spring Security also runs before
    // everything and would always fail if we didn't do this
    // CONT'D on lower comment
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .build();
    }

    // Not using `@Component` for the filters since doing so would cause the Filters to be Beans in the servlet chain
    // (), which would cause each to be checked twice (i.e. each filter regosters twice, and each request would go
    // through the authentication and authorization chains twice). Since we are building them here inside the
    // registration we avoid that.
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter(
            TokenVerifier verifier, RouteAccessPolicy policy) {
        var registration = new FilterRegistrationBean<>(new AuthenticationFilter(verifier, policy));
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilter(RouteAccessPolicy policy) {
        var registration = new FilterRegistrationBean<>(new AuthorizationFilter(policy));
        registration.setOrder(2);
        return registration;
    }
}
