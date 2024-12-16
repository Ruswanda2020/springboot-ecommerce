package com.oneDev.ecommerce.controller;

import com.oneDev.ecommerce.model.UserInfo;
import com.oneDev.ecommerce.model.request.AuthRequest;
import com.oneDev.ecommerce.model.request.UserRegisterRequest;
import com.oneDev.ecommerce.model.response.AuthResponse;
import com.oneDev.ecommerce.model.response.UserResponse;
import com.oneDev.ecommerce.service.AuthService;
import com.oneDev.ecommerce.service.JwtService;
import com.oneDev.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        UserInfo userInfo = authService.authenticate(authRequest);
        String token = jwtService.generateToken(userInfo);
        AuthResponse authResponse = AuthResponse.from(userInfo, token);
        return ResponseEntity.ok(authResponse);
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register( @Valid @RequestBody UserRegisterRequest UserRegisterRequest) {
        UserResponse userResponse = userService.register(UserRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
