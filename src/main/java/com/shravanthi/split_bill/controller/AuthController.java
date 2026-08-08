package com.shravanthi.split_bill.controller;

import com.shravanthi.split_bill.dto.AuthResponse;
import com.shravanthi.split_bill.dto.RegisterRequest;
import com.shravanthi.split_bill.service.AuthService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody RegisterRequest request) {
        return authService.login(request);
    }
}