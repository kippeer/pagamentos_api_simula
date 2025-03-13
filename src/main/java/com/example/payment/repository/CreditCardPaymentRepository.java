package com.example.payment.repository;

import com.example.payment.domain.model.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository extends JpaRepository<CreditCardPayment, String> {
    CreditCardPayment findByPaymentId(String paymentId);
}