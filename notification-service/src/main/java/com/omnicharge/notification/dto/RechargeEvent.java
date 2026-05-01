package com.omnicharge.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RechargeEvent {
    private Long id;
    private String rechargeId;
    private String mobileNumber;
    private String operatorName;
    private String planName;
    private BigDecimal amount;
    private Integer validityDays;
    private String status;
    private String transactionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
}
