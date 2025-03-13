package com.example.payment.exception;

import com.example.payment.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(PaymentValidationException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage(), "VALIDATION_ERROR"));
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessingException(PaymentProcessingException ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getMessage(), "PROCESSING_ERROR"));
    }

    @ExceptionHandler(InvalidCreditCardException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCreditCardException(InvalidCreditCardException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage(), "INVALID_CREDIT_CARD"));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), "PAYMENT_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStatusException(InvalidPaymentStatusException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage(), "INVALID_PAYMENT_STATUS"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse("Validation failed", "VALIDATION_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }
}