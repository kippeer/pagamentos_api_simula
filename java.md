# Payment System Implementation Guide

## Introduction
This guide describes a simulated payment processing system built with Spring Boot, implementing PIX, QR Code, and credit card payment methods. The system mimics real payment gateway behaviors without actual external API connections.

## System Architecture

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:adm}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Core Entities

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private BigDecimal amount;
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

public enum PaymentMethod {
    PIX, CREDIT_CARD, QR_CODE
}

public enum PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, EXPIRED
}

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
}

public enum NotificationType {
    EMAIL, SMS, WEBHOOK
}
```

### DTOs

```java
public record PaymentRequest(
    BigDecimal amount,
    String currency,
    PaymentMethod paymentMethod,
    Map<String, Object> paymentDetails,
    NotificationPreferences notificationPreferences
) {}

public record PaymentResponse(
    String id,
    PaymentStatus status,
    String paymentUrl,
    Map<String, Object> additionalInfo,
    LocalDateTime expiresAt
) {}

public record CreditCardRequest(
    String cardNumber,
    String cardHolderName,
    String expirationDate,
    String cvv,
    Integer installments,
    Boolean saveCard
) {}

public record PixRequest(
    String pixKey,
    LocalDateTime expiresAt,
    String description
) {}

public record NotificationPreferences(
    Boolean emailNotification,
    Boolean smsNotification,
    String webhookUrl,
    List<String> notifyOn
) {}

public record PaymentNotificationRequest(
    String paymentId,
    NotificationType type,
    String destination,
    Map<String, Object> metadata
) {}
```

### Service Layer Implementation

```java
@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CreditCardPaymentRepository creditCardPaymentRepository;
    private final PixPaymentRepository pixPaymentRepository;
    private final NotificationService notificationService;
    private final PaymentValidationService validationService;

    public PaymentResponse processPayment(PaymentRequest request) {
        validationService.validatePaymentRequest(request);
        Payment payment = createPayment(request);
        
        PaymentResponse response = switch (request.paymentMethod()) {
            case CREDIT_CARD -> processCreditCardPayment(payment, request);
            case PIX -> processPixPayment(payment, request);
            case QR_CODE -> processQrCodePayment(payment, request);
            default -> throw new UnsupportedPaymentMethodException();
        };
        
        scheduleNotifications(payment, request.notificationPreferences());
        return response;
    }

    private PaymentResponse processCreditCardPayment(Payment payment, PaymentRequest request) {
        // Simulate credit card validation and processing
        simulateProcessingDelay();
        
        CreditCardRequest cardDetails = extractCreditCardDetails(request);
        validateCreditCard(cardDetails);
        
        String cardNumberHash = hashCardNumber(cardDetails.cardNumber());
        String cardBrand = detectCardBrand(cardDetails.cardNumber());
        
        CreditCardPayment ccPayment = new CreditCardPayment();
        ccPayment.setPayment(payment);
        ccPayment.setCardNumberHash(cardNumberHash);
        ccPayment.setCardHolderName(cardDetails.cardHolderName());
        ccPayment.setInstallments(cardDetails.installments());
        ccPayment.setLastFourDigits(getLastFourDigits(cardDetails.cardNumber()));
        ccPayment.setCardBrand(cardBrand);
        ccPayment.setAuthorizationCode(generateAuthorizationCode());
        ccPayment.setTransactionId(generateTransactionId());
        ccPayment.setProcessedAt(LocalDateTime.now());
        
        creditCardPaymentRepository.save(ccPayment);
        
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        
        return new PaymentResponse(
            payment.getId(),
            PaymentStatus.COMPLETED,
            null,
            Map.of(
                "authorizationCode", ccPayment.getAuthorizationCode(),
                "transactionId", ccPayment.getTransactionId(),
                "cardBrand", cardBrand,
                "installments", cardDetails.installments()
            ),
            null
        );
    }

    private PaymentResponse processPixPayment(Payment payment, PaymentRequest request) {
        // Simulate PIX key generation and QR code creation
        PixRequest pixDetails = extractPixDetails(request);
        validatePixRequest(pixDetails);
        
        String qrCodeData = generatePixQrCode(payment.getId(), payment.getAmount());
        LocalDateTime expiresAt = pixDetails.expiresAt() != null 
            ? pixDetails.expiresAt() 
            : LocalDateTime.now().plusHours(24);
        
        PixPayment pixPayment = new PixPayment();
        pixPayment.setPayment(payment);
        pixPayment.setPixKey(pixDetails.pixKey());
        pixPayment.setQrCodeData(qrCodeData);
        pixPayment.setExpiresAt(expiresAt);
        pixPayment.setTransactionId(generateTransactionId());
        pixPayment.setPaid(false);
        
        pixPaymentRepository.save(pixPayment);
        
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        
        schedulePixExpirationCheck(payment.getId(), expiresAt);
        
        return new PaymentResponse(
            payment.getId(),
            PaymentStatus.PENDING,
            null,
            Map.of(
                "pixKey", pixDetails.pixKey(),
                "qrCodeData", qrCodeData,
                "transactionId", pixPayment.getTransactionId(),
                "description", pixDetails.description()
            ),
            expiresAt
        );
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkExpiredPayments() {
        List<Payment> pendingPayments = paymentRepository
            .findByStatusAndPaymentMethod(PaymentStatus.PENDING, PaymentMethod.PIX);
        
        for (Payment payment : pendingPayments) {
            PixPayment pixPayment = pixPaymentRepository.findByPaymentId(payment.getId())
                .orElseThrow();
                
            if (LocalDateTime.now().isAfter(pixPayment.getExpiresAt())) {
                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
                notificationService.sendPaymentExpiredNotification(payment);
            }
        }
    }

    public PaymentResponse refundPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException(paymentId));
            
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("Only completed payments can be refunded");
        }
        
        // Simulate refund processing
        simulateProcessingDelay();
        
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        
        notificationService.sendRefundNotification(payment);
        
        return new PaymentResponse(
            payment.getId(),
            PaymentStatus.REFUNDED,
            null,
            Map.of("refundedAt", LocalDateTime.now().toString()),
            null
        );
    }

    private void validateCreditCard(CreditCardRequest cardDetails) {
        if (!isValidCardNumber(cardDetails.cardNumber())) {
            throw new InvalidCreditCardException("Invalid card number");
        }
        
        if (!isValidExpirationDate(cardDetails.expirationDate())) {
            throw new InvalidCreditCardException("Card has expired");
        }
        
        if (cardDetails.installments() < 1 || cardDetails.installments() > 12) {
            throw new InvalidPaymentDetailsException("Invalid number of installments");
        }
    }

    private void validatePixRequest(PixRequest pixDetails) {
        if (pixDetails.expiresAt() != null && 
            pixDetails.expiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPaymentDetailsException("Expiration date must be in the future");
        }
    }

    private void scheduleNotifications(Payment payment, NotificationPreferences prefs) {
        if (prefs == null) return;
        
        if (prefs.emailNotification()) {
            notificationService.scheduleEmailNotification(payment);
        }
        
        if (prefs.smsNotification()) {
            notificationService.scheduleSmsNotification(payment);
        }
        
        if (prefs.webhookUrl() != null) {
            notificationService.scheduleWebhookNotification(payment, prefs.webhookUrl());
        }
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String generateAuthorizationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    private String generatePixQrCode(String paymentId, BigDecimal amount) {
        // Simulate QR code generation with payment information
        return String.format("PIX*%s*%s*%s", 
            paymentId,
            amount.toString(),
            LocalDateTime.now().toString()
        );
    }

    private String detectCardBrand(String cardNumber) {
        // Simplified card brand detection
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        return "UNKNOWN";
    }
}

@Service
@Transactional
public class NotificationService {
    private final PaymentNotificationRepository notificationRepository;
    private final AsyncNotificationSender notificationSender;

    public void scheduleEmailNotification(Payment payment) {
        PaymentNotification notification = createNotification(
            payment, 
            NotificationType.EMAIL
        );
        notificationRepository.save(notification);
        notificationSender.sendEmailAsync(notification);
    }

    public void scheduleSmsNotification(Payment payment) {
        PaymentNotification notification = createNotification(
            payment, 
            NotificationType.SMS
        );
        notificationRepository.save(notification);
        notificationSender.sendSmsAsync(notification);
    }

    public void scheduleWebhookNotification(Payment payment, String webhookUrl) {
        PaymentNotification notification = createNotification(
            payment, 
            NotificationType.WEBHOOK
        );
        notification.setWebhookUrl(webhookUrl);
        notificationRepository.save(notification);
        notificationSender.sendWebhookAsync(notification);
    }

    private PaymentNotification createNotification(Payment payment, NotificationType type) {
        PaymentNotification notification = new PaymentNotification();
        notification.setPayment(payment);
        notification.setType(type);
        notification.setSentAt(LocalDateTime.now());
        notification.setSuccessful(false);
        return notification;
    }
}
```

### Controllers

```java
@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentQueryService queryService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
        @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
        return queryService.getPayment(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String id) {
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PaymentResponse>> searchPayments(
        @RequestParam(required = false) PaymentStatus status,
        @RequestParam(required = false) PaymentMethod method,
        @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<PaymentResponse> payments = queryService.searchPayments(
            status, method, startDate, endDate, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<PaymentNotification>> getPaymentNotifications(
        @PathVariable String id
    ) {
        List<PaymentNotification> notifications = queryService.getPaymentNotifications(id);
        return ResponseEntity.ok(notifications);
    }
}

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @PostMapping("/pix/{paymentId}")
    public ResponseEntity<Void> handlePixCallback(
        @PathVariable String paymentId,
        @RequestBody PixCallbackRequest callback
    ) {
        paymentService.handlePixCallback(paymentId, callback);
        return ResponseEntity.ok().build();
    }
}
```

## Testing

### Integration Tests

```java
@SpringBootTest
class PaymentIntegrationTest {
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Test
    void creditCardPaymentShouldComplete() {
        PaymentRequest request = new PaymentRequest(
            new BigDecimal("100.00"),
            "BRL",
            PaymentMethod.CREDIT_CARD,
            Map.of(
                "cardNumber", "4111111111111111",
                "cardHolderName", "John Doe",
                "expirationDate", "12/25",
                "cvv", "123",
                "installments", 1
            ),
            new NotificationPreferences(true, false, null, List.of("COMPLETED"))
        );
        
        PaymentResponse response = paymentService.processPayment(request);
        
        assertNotNull(response.id());
        assertEquals(PaymentStatus.COMPLETED, response.status());
        assertNotNull(response.additionalInfo().get("authorizationCode"));
        assertNotNull(response.additionalInfo().get("transactionId"));
    }
    
    @Test
    void pixPaymentShouldGenerateQrCode() {
        PaymentRequest request = new PaymentRequest(
            new BigDecimal("50.00"),
            "BRL",
            PaymentMethod.PIX,
            Map.of(
                "pixKey", "test@email.com",
                "expiresAt", LocalDateTime.now().plusHours(1),
                "description", "Test payment"
            ),
            new NotificationPreferences(true, true, null, List.of("COMPLETED", "EXPIRED"))
        );
        
        PaymentResponse response = paymentService.processPayment(request);
        
        assertNotNull(response.id());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertNotNull(response.additionalInfo().get("qrCodeData"));
        assertNotNull(response.additionalInfo().get("pixKey"));
        assertNotNull(response.expiresAt());
    }
    
    @Test
    void shouldHandlePaymentRefund() {
        // First create and complete a payment
        PaymentRequest request = new PaymentRequest(
            new BigDecimal("100.00"),
            "BRL",
            PaymentMethod.CREDIT_CARD,
            Map.of(
                "cardNumber", "4111111111111111",
                "cardHolderName", "John Doe",
                "expirationDate", "12/25",
                "cvv", "123",
                "installments", 1
            ),
            null
        );
        
        PaymentResponse payment = paymentService.processPayment(request);
        
        // Then refund it
        PaymentResponse refundResponse = paymentService.refundPayment(payment.id());
        
        assertEquals(PaymentStatus.REFUNDED, refundResponse.status());
        assertNotNull(refundResponse.additionalInfo().get("refundedAt"));
    }
}
```

## Security Considerations

1. **Data Encryption**
   - All sensitive data (card numbers, CVV) should be encrypted in transit (HTTPS)
   - Card numbers should be hashed before storage
   - Only store last 4 digits of card numbers
   - Use strong encryption algorithms for sensitive data
   - Implement key rotation policies

2. **Authentication & Authorization**
   - Implement JWT-based authentication
   - Role-based access control for payment operations
   - Rate limiting for API endpoints
   - IP-based blocking for suspicious activities
   - Session management and timeout policies

3. **Validation**
   - Input validation for all payment details
   - Amount range validation
   - Card number validation using Luhn algorithm
   - Expiration date validation
   - PIX key format validation
   - QR code data validation

4. **Audit & Logging**
   - Maintain detailed audit logs
   - Log all payment status changes
   - Track user actions and system events
   - Implement log rotation and retention policies

## Error Handling

```java
@ControllerAdvice
public class PaymentExceptionHandler {
    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(PaymentValidationException ex) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage(), "VALIDATION_ERROR"));
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessingException(PaymentProcessingException ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getMessage(), "PROCESSING_ERROR"));
    }

    @ExceptionHandler(InvalidCreditCardException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCreditCardException(
        InvalidCreditCardException ex
    ) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage(), "INVALID_CREDIT_CARD"));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(
        PaymentNotFoundException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), "PAYMENT_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStatusException(
        InvalidPaymentStatusException ex
    ) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(ex.getMessage(), "INVALID_PAYMENT_STATUS"));
    }
}
```

## Monitoring and Metrics

```java
@Configuration
public class MetricsConfig {
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}

@Aspect
@Component
public class PaymentMetricsAspect {
    private final MeterRegistry meterRegistry;

    @Around("execution(* com.example.payment.service.PaymentService.processPayment(..))")
    public Object measurePaymentProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(Timer.builder("payment.processing")
                .tag("method", joinPoint.getSignature().getName())
                .register(meterRegistry));
        }
    }

    @AfterReturning("execution(* com.example.payment.service.PaymentService.processPayment(..))")
    public void recordSuccessfulPayment(JoinPoint joinPoint) {
        PaymentRequest request = (PaymentRequest) joinPoint.getArgs()[0];
        meterRegistry.counter("payment.success",
            Tags.of("method", request.paymentMethod().toString())
        ).increment();
    }

    @AfterThrowing("execution(* com.example.payment.service.PaymentService.processPayment(..))")
    public void recordFailedPayment(JoinPoint joinPoint) {
        PaymentRequest request = (PaymentRequest) joinPoint.getArgs()[0];
        meterRegistry.counter("payment.failure",
            Tags.of("method", request.paymentMethod().toString())
        ).increment();
    }
}
```

This implementation provides a robust foundation for a simulated payment system. It includes all major components needed for processing different payment methods, with proper error handling, security considerations, and monitoring capabilities. The system can be extended with additional payment methods or integrated with real payment gateways in the future.