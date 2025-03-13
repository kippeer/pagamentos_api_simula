package com.example.payment.repository;

import com.example.payment.domain.model.PaymentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, String> {
    List<PaymentNotification> findByPaymentId(String paymentId);
}