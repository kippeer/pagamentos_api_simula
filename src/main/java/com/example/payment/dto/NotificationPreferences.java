package com.example.payment.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationPreferences {
    private Boolean emailNotification = false;
    private Boolean smsNotification = false;
    private String webhookUrl;
    private List<String> notifyOn;
}