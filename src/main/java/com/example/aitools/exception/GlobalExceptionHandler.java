package com.example.aitools.exception;

import com.example.aitools.common.R;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        if (e.getMessage().contains("users.uk_email")) {
            return R.error("邮箱已被注册");
        }
        if (e.getMessage().contains("users.uk_username")) {
            return R.error("用户名已被使用");
        }
        return R.error("数据库操作异常");
    }
} 