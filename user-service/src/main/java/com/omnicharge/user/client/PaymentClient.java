package com.omnicharge.user.client;

import com.omnicharge.user.client.fallback.PaymentClientFallback;
import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @GetMapping("/api/payments/user/{userId}")
    List<TransactionResponse> getTransactionsByUserId(
            @PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/payments/transaction/{transactionId}")
    TransactionResponse getTransactionById(
            @PathVariable String transactionId,
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/payments/recharge/{rechargeId}")
    TransactionResponse getTransactionByRechargeId(
            @PathVariable String rechargeId,
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authHeader);

    @org.springframework.web.bind.annotation.DeleteMapping("/api/payments/internal/user/{userId}")
    void deleteAllTransactionsForUser(
            @PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestHeader("X-Internal-Secret") String internalSecret);

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class TransactionResponse {
        private Long id;
        private String transactionId;
        private String rechargeId;
        private Long userId;
        private BigDecimal amount;
        private String description;
        private String status;
        private String paymentMethod;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
    }
}
