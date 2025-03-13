package com.example.payment.exception;

public class InvalidCreditCardException extends PaymentValidationException {
    public InvalidCreditCardException(String message) {
        super(message);
    }
}