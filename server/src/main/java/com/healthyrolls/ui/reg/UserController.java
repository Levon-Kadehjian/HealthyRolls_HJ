package com.healthyrolls.ui.reg;

import static org.springframework.http.HttpStatus.CREATED;

import com.healthyrolls.application.reg.LoginResult;
import com.healthyrolls.application.reg.UserService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.common.UserId;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        UserId id = userService.register(req.email(), req.hashedPassword(), req.name(), req.role());
        return ResponseEntity.status(CREATED).body(UserResponse.from(userService.findById(id)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResult r = userService.login(req.email(), req.hashedPassword());
        return ResponseEntity.ok(new AuthResponse(
                r.token().value(), r.token().expiresAt(), r.userId(), r.role()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser current) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(current.userId())));
    }
}
