package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String code;
    private LocalDateTime timestamp;
    
    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
}