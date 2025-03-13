package com.example.payment.domain.model;

import com.example.payment.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_notifications")
public class PaymentNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    private Payment payment;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private String message;
    private LocalDateTime sentAt;
    private Boolean successful;
    private String errorDetails;
    private String webhookUrl;
}