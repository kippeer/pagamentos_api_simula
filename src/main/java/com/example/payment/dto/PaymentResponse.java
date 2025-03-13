package com.example.payment.dto;

import com.example.payment.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PaymentResponse {
    private String id;
    private PaymentStatus status;
    private String paymentUrl;
    private Map<String, Object> additionalInfo;
    private LocalDateTime expiresAt;
}