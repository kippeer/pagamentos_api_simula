package com.example.payment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PixCallbackRequest {
    private String transactionId;
    private LocalDateTime paidAt;
    private String payerPixKey;
    private String payerBank;
}