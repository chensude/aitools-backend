package com.example.aitools.model.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;
} 