package com.example.payment.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class NotificationPreferences {
    private Boolean emailNotification = false;
    private Boolean smsNotification = false;
    
    @Pattern(regexp = "^https?://.*$", message = "Webhook URL must be a valid HTTP(S) URL")
    private String webhookUrl;
    
    private List<String> notifyOn;
}