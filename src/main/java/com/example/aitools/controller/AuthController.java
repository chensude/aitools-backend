package com.example.aitools.controller;

import com.example.aitools.common.R;
import com.example.aitools.model.request.RegisterRequest;
import com.example.aitools.model.request.LoginRequest;
import com.example.aitools.model.response.LoginResponse;
import com.example.aitools.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Email;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/send-code")
    public R<Void> sendCode(@RequestParam @Email String email) {
        userService.sendVerificationCode(email);
        return R.ok();
    }
    
    @PostMapping("/register")
    public R<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            request.getCode()
        );
        return R.ok();
    }
    
    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        String token = userService.login(request.getEmail(), request.getPassword());
        return R.ok(new LoginResponse(token));
    }
} 