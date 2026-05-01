package com.omnicharge.recharge.client;

import com.omnicharge.recharge.client.fallback.PaymentClientFallback;
import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/api/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class PaymentRequest {
        private String rechargeId;
        private Long userId;
        private BigDecimal amount;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class PaymentResponse {
        private String transactionId;
        private String status;
        private String message;
    }
}
