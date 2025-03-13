package com.example.payment.exception;

public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException(String paymentId) {
        super("Payment not found with ID: " + paymentId);
    }
}