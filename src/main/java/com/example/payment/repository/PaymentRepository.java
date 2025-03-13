package com.example.payment.repository;

import com.example.payment.domain.enums.PaymentMethod;
import com.example.payment.domain.enums.PaymentStatus;
import com.example.payment.domain.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByStatusAndPaymentMethod(PaymentStatus status, PaymentMethod method);
    
    Page<Payment> findByStatusAndPaymentMethodAndCreatedAtBetween(
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
}