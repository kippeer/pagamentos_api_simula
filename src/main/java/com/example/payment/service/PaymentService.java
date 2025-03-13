package com.example.payment.service;

import com.example.payment.domain.enums.PaymentMethod;
import com.example.payment.domain.enums.PaymentStatus;
import com.example.payment.domain.model.CreditCardPayment;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PixPayment;
import com.example.payment.dto.*;
import com.example.payment.exception.InvalidPaymentStatusException;
import com.example.payment.exception.PaymentNotFoundException;
import com.example.payment.exception.PaymentProcessingException;
import com.example.payment.repository.CreditCardPaymentRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.PixPaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CreditCardPaymentRepository creditCardPaymentRepository;
    private final PixPaymentRepository pixPaymentRepository;
    private final NotificationService notificationService;
    private final PaymentValidationService validationService;
    private final ObjectMapper objectMapper;

    public PaymentService(
        PaymentRepository paymentRepository,
        CreditCardPaymentRepository creditCardPaymentRepository,
        PixPaymentRepository pixPaymentRepository,
        NotificationService notificationService,
        PaymentValidationService validationService,
        ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.creditCardPaymentRepository = creditCardPaymentRepository;
        this.pixPaymentRepository = pixPaymentRepository;
        this.notificationService = notificationService;
        this.validationService = validationService;
        this.objectMapper = objectMapper;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        validationService.validatePaymentRequest(request);
        
        Payment payment = createPayment(request);
        payment = paymentRepository.save(payment);
        
        PaymentResponse response = switch (request.getPaymentMethod()) {
            case CREDIT_CARD -> processCreditCardPayment(payment, request);
            case PIX -> processPixPayment(payment, request);
            case QR_CODE -> processQrCodePayment(payment, request);
            default -> throw new PaymentProcessingException("Unsupported payment method");
        };
        
        scheduleNotifications(payment, request.getNotificationPreferences());
        return response;
    }

    private Payment createPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    private PaymentResponse processCreditCardPayment(Payment payment, PaymentRequest request) {
        simulateProcessingDelay();
        
        CreditCardRequest cardDetails = objectMapper.convertValue(
            request.getPaymentDetails(), 
            CreditCardRequest.class
        );
        
        String cardNumberHash = hashCardNumber(cardDetails.getCardNumber());
        String cardBrand = detectCardBrand(cardDetails.getCardNumber());
        
        CreditCardPayment ccPayment = new CreditCardPayment();
        ccPayment.setPayment(payment);
        ccPayment.setCardNumberHash(cardNumberHash);
        ccPayment.setCardHolderName(cardDetails.getCardHolderName());
        ccPayment.setInstallments(cardDetails.getInstallments());
        ccPayment.setLastFourDigits(getLastFourDigits(cardDetails.getCardNumber()));
        ccPayment.setCardBrand(cardBrand);
        ccPayment.setAuthorizationCode(generateAuthorizationCode());
        ccPayment.setTransactionId(generateTransactionId());
        ccPayment.setProcessedAt(LocalDateTime.now());
        
        creditCardPaymentRepository.save(ccPayment);
        
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        
        return PaymentResponse.builder()
            .id(payment.getId())
            .status(PaymentStatus.COMPLETED)
            .additionalInfo(Map.of(
                "authorizationCode", ccPayment.getAuthorizationCode(),
                "transactionId", ccPayment.getTransactionId(),
                "cardBrand", cardBrand,
                "installments", cardDetails.getInstallments()
            ))
            .build();
    }

    private PaymentResponse processPixPayment(Payment payment, PaymentRequest request) {
        PixRequest pixDetails = objectMapper.convertValue(
            request.getPaymentDetails(), 
            PixRequest.class
        );
        
        String qrCodeData = generatePixQrCode(payment.getId(), payment.getAmount());
        LocalDateTime expiresAt = pixDetails.getExpiresAt() != null 
            ? pixDetails.getExpiresAt() 
            : LocalDateTime.now().plusHours(24);
        
        PixPayment pixPayment = new PixPayment();
        pixPayment.setPayment(payment);
        pixPayment.setPixKey(pixDetails.getPixKey());
        pixPayment.setQrCodeData(qrCodeData);
        pixPayment.setExpiresAt(expiresAt);
        pixPayment.setTransactionId(generateTransactionId());
        pixPayment.setPaid(false);
        
        pixPaymentRepository.save(pixPayment);
        
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        
        return PaymentResponse.builder()
            .id(payment.getId())
            .status(PaymentStatus.PENDING)
            .additionalInfo(Map.of(
                "pixKey", pixDetails.getPixKey(),
                "qrCodeData", qrCodeData,
                "transactionId", pixPayment.getTransactionId(),
                "description", pixDetails.getDescription()
            ))
            .expiresAt(expiresAt)
            .build();
    }

    private PaymentResponse processQrCodePayment(Payment payment, PaymentRequest request) {
        // Implement QR code payment processing
        throw new PaymentProcessingException("QR code payment processing not implemented yet");
    }

    public PaymentResponse refundPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
            
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("Only completed payments can be refunded");
        }
        
        simulateProcessingDelay();
        
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        
        notificationService.sendRefundNotification(payment);
        
        return PaymentResponse.builder()
            .id(payment.getId())
            .status(PaymentStatus.REFUNDED)
            .additionalInfo(Map.of("refundedAt", LocalDateTime.now().toString()))
            .build();
    }

    public void handlePixCallback(String paymentId, PixCallbackRequest callback) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
            
        PixPayment pixPayment = pixPaymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentProcessingException("PIX payment not found"));
            
        if (Boolean.TRUE.equals(pixPayment.getPaid())) {
            throw new PaymentProcessingException("Payment already processed");
        }
        
        pixPayment.setPaid(true);
        pixPayment.setPaidAt(LocalDateTime.now());
        pixPaymentRepository.save(pixPayment);
        
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        
        scheduleNotifications(payment, null);
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkExpiredPayments() {
        List<Payment> pendingPayments = paymentRepository
            .findByStatusAndPaymentMethod(PaymentStatus.PENDING, PaymentMethod.PIX);
        
        for (Payment payment : pendingPayments) {
            PixPayment pixPayment = pixPaymentRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new PaymentProcessingException("PIX payment not found"));
                
            if (LocalDateTime.now().isAfter(pixPayment.getExpiresAt())) {
                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
                notificationService.sendPaymentExpiredNotification(payment);
            }
        }
    }

    private void scheduleNotifications(Payment payment, NotificationPreferences prefs) {
        if (prefs == null) return;
        
        if (Boolean.TRUE.equals(prefs.getEmailNotification())) {
            notificationService.scheduleEmailNotification(payment);
        }
        
        if (Boolean.TRUE.equals(prefs.getSmsNotification())) {
            notificationService.scheduleSmsNotification(payment);
        }
        
        if (prefs.getWebhookUrl() != null) {
            notificationService.scheduleWebhookNotification(payment, prefs.getWebhookUrl());
        }
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProcessingException("Payment processing interrupted");
        }
    }

    private String hashCardNumber(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to hash card number");
        }
    }

    private String getLastFourDigits(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private String generateAuthorizationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    private String generatePixQrCode(String paymentId, java.math.BigDecimal amount) {
        return String.format("PIX*%s*%s*%s", 
            paymentId,
            amount.toString(),
            LocalDateTime.now().toString()
        );
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        return "UNKNOWN";
    }
}