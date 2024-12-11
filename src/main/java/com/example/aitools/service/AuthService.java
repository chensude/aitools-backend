package com.example.aitools.service;

import com.example.aitools.entity.User;
import com.example.aitools.model.request.LoginRequest;
import com.example.aitools.model.response.LoginResponse;
import com.example.aitools.repository.UserRepository;
import com.example.aitools.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
            
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId());
        
        return LoginResponse.builder()
            .token(token)
            .userId(user.getId())
            .username(user.getUsername())
            .build();
    }
} 