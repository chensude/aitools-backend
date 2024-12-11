package com.example.aitools.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableRetry
@EnableAspectJAutoProxy
public class RetryConfig {
} 