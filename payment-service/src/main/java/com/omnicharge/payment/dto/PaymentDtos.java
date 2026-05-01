package com.omnicharge.payment.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentRequest {
        @NotBlank
        private String rechargeId;
        @NotNull
        private Long userId;
        @NotNull @DecimalMin("1.0")
        private BigDecimal amount;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentResponse {
        private String transactionId;
        private String status;
        private String message;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TransactionResponse {
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
