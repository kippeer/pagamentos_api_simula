package com.example.payment.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pix_payments")
public class PixPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne
    private Payment payment;
    
    private String pixKey;
    private String qrCodeData;
    private LocalDateTime expiresAt;
    private String transactionId;
    private Boolean paid;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}