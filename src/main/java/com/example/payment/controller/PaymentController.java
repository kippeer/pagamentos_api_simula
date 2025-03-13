package com.example.payment.controller;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.model.PaymentNotification;
import com.example.payment.service.PaymentService;
import com.example.payment.service.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Operations", description = "APIs for payment processing and management")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentQueryService queryService;

    @Operation(
        summary = "Create a new payment",
        description = "Process a new payment using PIX, Credit Card, or QR Code"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Payment processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid payment request"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during payment processing"
        )
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
        @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get payment details",
        description = "Retrieve details of a specific payment by ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
        @Parameter(description = "Payment ID") @PathVariable String id
    ) {
        return queryService.getPayment(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Refund payment",
        description = "Process a refund for a completed payment"
    )
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
        @Parameter(description = "Payment ID") @PathVariable String id
    ) {
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Search payments",
        description = "Search payments with various filters and pagination"
    )
    @GetMapping("/search")
    public ResponseEntity<Page<PaymentResponse>> searchPayments(
        @Parameter(description = "Payment status filter")
        @RequestParam(required = false) PaymentStatus status,
        
        @Parameter(description = "Payment method filter")
        @RequestParam(required = false) PaymentMethod method,
        
        @Parameter(description = "Start date for payment search")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,
        
        @Parameter(description = "End date for payment search")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate,
        
        @Parameter(description = "Page number")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<PaymentResponse> payments = queryService.searchPayments(
            status, method, startDate, endDate, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(payments);
    }

    @Operation(
        summary = "Get payment notifications",
        description = "Retrieve all notifications for a specific payment"
    )
    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<PaymentNotification>> getPaymentNotifications(
        @Parameter(description = "Payment ID") @PathVariable String id
    ) {
        List<PaymentNotification> notifications = queryService.getPaymentNotifications(id);
        return ResponseEntity.ok(notifications);
    }
}