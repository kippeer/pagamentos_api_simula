package com.example.payment.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "credit_card_payments")
public class CreditCardPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne
    private Payment payment;
    
    private String cardNumberHash;
    private String cardHolderName;
    private Integer installments;
    private String lastFourDigits;
    private String cardBrand;
    private String authorizationCode;
    private String transactionId;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}