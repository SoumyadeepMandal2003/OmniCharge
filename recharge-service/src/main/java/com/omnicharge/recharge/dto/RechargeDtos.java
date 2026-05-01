package com.omnicharge.recharge.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RechargeDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RechargeRequest {
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
        private String mobileNumber;

        @NotNull
        private Long operatorId;

        @NotNull
        private Long planId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RechargeResponse {
        private Long id;
        private String rechargeId;
        private String mobileNumber;
        private String operatorName;
        private String planName;
        private BigDecimal amount;
        private Integer validityDays;
        private String status;
        private String transactionId;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlanDto {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer validityDays;
        private String data;
        private String calls;
        private String type;
        private Long operatorId;
        private String operatorName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OperatorDto {
        private Long id;
        private String name;
        private String code;
    }
}
