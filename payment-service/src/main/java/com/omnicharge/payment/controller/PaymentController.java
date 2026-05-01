package com.omnicharge.payment.controller;

import com.omnicharge.payment.dto.PaymentDtos.*;
import com.omnicharge.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "Payment processing and transaction management")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process a payment (internal - called by recharge-service)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get transaction by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getTransactionById(transactionId));
    }

    @GetMapping("/recharge/{rechargeId}")
    @Operation(summary = "Get transaction by recharge ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TransactionResponse> getByRechargeId(@PathVariable String rechargeId) {
        return ResponseEntity.ok(paymentService.getTransactionByRechargeId(rechargeId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all transactions for a user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getTransactionsByUser(userId));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transactions (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(paymentService.getAllTransactions());
    }
}
