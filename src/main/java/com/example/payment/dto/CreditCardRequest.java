package com.example.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreditCardRequest {
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Invalid card number format")
    private String cardNumber;
    
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;
    
    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Invalid expiration date format (MM/YY)")
    private String expirationDate;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV format")
    private String cvv;
    
    private Integer installments = 1;
    private Boolean saveCard = false;
}