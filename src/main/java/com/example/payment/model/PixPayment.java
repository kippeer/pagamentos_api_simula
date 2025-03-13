package com.example.payment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pix_payments")
@EqualsAndHashCode(of = "id")
public class PixPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    
    @Column(nullable = false)
    private String pixKey;
    
    @Column(nullable = false)
    private String qrCodeData;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    private String transactionId;
    
    @Column(nullable = false)
    private Boolean paid = false;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}