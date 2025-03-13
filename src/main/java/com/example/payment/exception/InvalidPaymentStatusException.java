package com.example.payment.exception;

public class InvalidPaymentStatusException extends PaymentValidationException {
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}