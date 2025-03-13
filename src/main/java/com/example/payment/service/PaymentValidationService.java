package com.example.payment.service;

import com.example.payment.dto.CreditCardRequest;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PixRequest;
import com.example.payment.exception.InvalidCreditCardException;
import com.example.payment.exception.PaymentValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentValidationService {
    private final ObjectMapper objectMapper;

    public PaymentValidationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validatePaymentRequest(PaymentRequest request) {
        switch (request.getPaymentMethod()) {
            case CREDIT_CARD -> validateCreditCardPayment(request);
            case PIX -> validatePixPayment(request);
            case QR_CODE -> validateQrCodePayment(request);
            default -> throw new PaymentValidationException("Unsupported payment method");
        }
    }

    private void validateCreditCardPayment(PaymentRequest request) {
        CreditCardRequest cardDetails = objectMapper.convertValue(
            request.getPaymentDetails(), 
            CreditCardRequest.class
        );
        
        if (!isValidCardNumber(cardDetails.getCardNumber())) {
            throw new InvalidCreditCardException("Invalid card number");
        }
        
        if (!isValidExpirationDate(cardDetails.getExpirationDate())) {
            throw new InvalidCreditCardException("Card has expired");
        }
        
        if (cardDetails.getInstallments() < 1 || cardDetails.getInstallments() > 12) {
            throw new PaymentValidationException("Invalid number of installments");
        }
    }

    private void validatePixPayment(PaymentRequest request) {
        PixRequest pixDetails = objectMapper.convertValue(
            request.getPaymentDetails(), 
            PixRequest.class
        );
        
        if (pixDetails.getExpiresAt() != null && 
            pixDetails.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PaymentValidationException("Expiration date must be in the future");
        }
    }

    private void validateQrCodePayment(PaymentRequest request) {
        // Add QR code specific validations if needed
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        
        // Luhn algorithm implementation
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }

    private boolean isValidExpirationDate(String expirationDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expirationDate, formatter);
            return !expiry.isBefore(YearMonth.now());
        } catch (Exception e) {
            return false;
        }
    }
}