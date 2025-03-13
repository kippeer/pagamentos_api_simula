package com.example.payment.service;

import com.example.payment.domain.enums.PaymentMethod;
import com.example.payment.domain.enums.PaymentStatus;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PaymentNotification;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.exception.PaymentNotFoundException;
import com.example.payment.repository.PaymentNotificationRepository;
import com.example.payment.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PaymentQueryService {
    private final PaymentRepository paymentRepository;
    private final PaymentNotificationRepository notificationRepository;

    public PaymentQueryService(
        PaymentRepository paymentRepository,
        PaymentNotificationRepository notificationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
    }

    public Optional<PaymentResponse> getPayment(String id) {
        return paymentRepository.findById(id)
            .map(this::convertToResponse);
    }

    public Page<PaymentResponse> searchPayments(
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        Page<Payment> payments = paymentRepository
            .findByStatusAndPaymentMethodAndCreatedAtBetween(
                status, 
                method, 
                startDate != null ? startDate : LocalDateTime.MIN,
                endDate != null ? endDate : LocalDateTime.now(),
                pageable
            );
            
        return payments.map(this::convertToResponse);
    }

    public List<PaymentNotification> getPaymentNotifications(String paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new PaymentNotFoundException(paymentId);
        }
        return notificationRepository.findByPaymentId(paymentId);
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .status(payment.getStatus())
            .additionalInfo(new HashMap<>())
            .build();
    }
}