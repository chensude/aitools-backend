package com.example.aitools.controller;

import com.example.aitools.common.R;
import com.example.aitools.model.request.LoginRequest;
import com.example.aitools.model.request.RegisterRequest;
import com.example.aitools.model.response.LoginResponse;
import com.example.aitools.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public R<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return R.success();
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request.getUsername(), request.getPassword());
            return R.success(LoginResponse.builder().token(token).build());
        } catch (RuntimeException e) {
            return R.error(401, e.getMessage());  // 使用401表示认证失败
        } catch (Exception e) {
            return R.error(500, "系统异常");
        }
    }
} 