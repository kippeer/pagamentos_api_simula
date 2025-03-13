package com.example.payment.repository;

import com.example.payment.domain.model.PixPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PixPaymentRepository extends JpaRepository<PixPayment, String> {
    Optional<PixPayment> findByPaymentId(String paymentId);
}