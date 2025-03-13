package com.example.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "credit_card_payments")
@EqualsAndHashCode(of = "id")
public class CreditCardPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    
    @Column(nullable = false)
    private String cardNumberHash;
    
    @Column(nullable = false)
    private String cardHolderName;
    
    @Column(nullable = false)
    private Integer installments;
    
    @Column(nullable = false, length = 4)
    private String lastFourDigits;
    
    @Column(nullable = false)
    private String cardBrand;
    
    private String authorizationCode;
    
    private String transactionId;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}