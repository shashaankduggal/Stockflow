package com.stockflow.auth;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);

    }

    @PostMapping("/signup")
    public LoginResponse signup(
            @Valid @RequestBody SignupRequest request) {

        return authService.signup(request);

    }

}
