package com.example.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PixRequest {
    @NotBlank(message = "PIX key is required")
    private String pixKey;
    private LocalDateTime expiresAt;
    private String description;
}