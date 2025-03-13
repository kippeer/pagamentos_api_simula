package com.example.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreditCardRequest {
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String cardNumber;
    
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;
    
    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiration date must be in MM/YY format")
    private String expirationDate;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;
    
    @Min(value = 1, message = "Minimum installment is 1")
    @Max(value = 12, message = "Maximum installment is 12")
    private Integer installments = 1;
    
    private Boolean saveCard = false;
}