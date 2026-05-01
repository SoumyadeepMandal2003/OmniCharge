package com.omnicharge.operator.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

public class OperatorDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OperatorRequest {
        @NotBlank @Size(max = 50)
        private String name;
        @NotBlank @Size(max = 10)
        private String code;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OperatorResponse {
        private Long id;
        private String name;
        private String code;
        private String description;
        private boolean active;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlanRequest {
        @NotBlank @Size(max = 100)
        private String name;
        @NotNull @DecimalMin("1.0")
        private BigDecimal price;
        @NotNull @Min(1)
        private Integer validityDays;
        private String data;
        private String calls;
        private String sms;
        private String description;
        @NotNull
        private String type;
        @NotNull
        private Long operatorId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlanResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer validityDays;
        private String data;
        private String calls;
        private String sms;
        private String description;
        private String type;
        private boolean active;
        private Long operatorId;
        private String operatorName;
    }
}
