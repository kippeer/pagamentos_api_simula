package com.example.payment.dto;

import com.example.payment.domain.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Valid
    @NotNull(message = "Payment details are required")
    private Map<String, Object> paymentDetails;
    
    @Valid
    private NotificationPreferences notificationPreferences;
}