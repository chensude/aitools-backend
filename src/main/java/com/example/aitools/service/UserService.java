package com.example.aitools.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void sendVerificationCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 保存验证码
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(5));
        verificationCodeRepository.save(verificationCode);
        
        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@aitools.com");
        message.setTo(email);
        message.setSubject("AI Tools Hub 验证码");
        message.setText("您的验证码是: " + code + "\n验证码5分钟内有效。");
        mailSender.send(message);
    }
    
    public void register(String username, String email, String password, String code) {
        // 验证验证码
        VerificationCode verificationCode = verificationCodeRepository
            .findFirstByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new RuntimeException("验证码不存在"));
            
        if (verificationCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("验证码已过期");
        }
        
        if (!verificationCode.getCode().equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);
        userRepository.save(user);
    }
    
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
            
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 生成JWT token
        return JwtUtil.generateToken(user.getId());
    }
} 