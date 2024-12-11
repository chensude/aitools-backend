package com.example.aitools.repository;

import com.example.aitools.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findFirstByEmailOrderByCreatedAtDesc(String email);
} 