package com.example.aitools.model.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Data
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private Long userId;
}



