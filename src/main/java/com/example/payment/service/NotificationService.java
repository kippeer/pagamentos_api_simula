package com.example.payment.service;

import com.example.payment.domain.enums.NotificationType;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.model.PaymentNotification;
import com.example.payment.repository.PaymentNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class NotificationService {
    private final PaymentNotificationRepository notificationRepository;

    public NotificationService(PaymentNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Async
    public void scheduleEmailNotification(Payment payment) {
        PaymentNotification notification = createNotification(payment, NotificationType.EMAIL);
        sendEmailAsync(notification);
    }

    @Async
    public void scheduleSmsNotification(Payment payment) {
        PaymentNotification notification = createNotification(payment, NotificationType.SMS);
        sendSmsAsync(notification);
    }

    @Async
    public void scheduleWebhookNotification(Payment payment, String webhookUrl) {
        PaymentNotification notification = createNotification(payment, NotificationType.WEBHOOK);
        notification.setWebhookUrl(webhookUrl);
        sendWebhookAsync(notification);
    }

    public void sendPaymentExpiredNotification(Payment payment) {
        PaymentNotification notification = createNotification(payment, NotificationType.EMAIL);
        notification.setMessage("Payment has expired");
        notificationRepository.save(notification);
        sendEmailAsync(notification);
    }

    public void sendRefundNotification(Payment payment) {
        PaymentNotification notification = createNotification(payment, NotificationType.EMAIL);
        notification.setMessage("Payment has been refunded");
        notificationRepository.save(notification);
        sendEmailAsync(notification);
    }

    private PaymentNotification createNotification(Payment payment, NotificationType type) {
        PaymentNotification notification = new PaymentNotification();
        notification.setPayment(payment);
        notification.setType(type);
        notification.setSentAt(LocalDateTime.now());
        notification.setSuccessful(false);
        return notificationRepository.save(notification);
    }

    private void sendEmailAsync(PaymentNotification notification) {
        try {
            // Simulate email sending
            Thread.sleep(1000);
            notification.setSuccessful(true);
            notification.setMessage("Email notification sent successfully");
        } catch (Exception e) {
            notification.setSuccessful(false);
            notification.setErrorDetails(e.getMessage());
            log.error("Failed to send email notification", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    private void sendSmsAsync(PaymentNotification notification) {
        try {
            // Simulate SMS sending
            Thread.sleep(1000);
            notification.setSuccessful(true);
            notification.setMessage("SMS notification sent successfully");
        } catch (Exception e) {
            notification.setSuccessful(false);
            notification.setErrorDetails(e.getMessage());
            log.error("Failed to send SMS notification", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    private void sendWebhookAsync(PaymentNotification notification) {
        try {
            // Simulate webhook call
            Thread.sleep(1000);
            notification.setSuccessful(true);
            notification.setMessage("Webhook notification sent successfully");
        } catch (Exception e) {
            notification.setSuccessful(false);
            notification.setErrorDetails(e.getMessage());
            log.error("Failed to send webhook notification", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    public List<PaymentNotification> getPaymentNotifications(String paymentId) {
        return notificationRepository.findByPaymentId(paymentId);
    }
}