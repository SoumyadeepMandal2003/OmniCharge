package com.omnicharge.user.client;

import com.omnicharge.user.client.fallback.RechargeClientFallback;
import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "recharge-service", fallback = RechargeClientFallback.class)
public interface RechargeClient {

    @GetMapping("/api/recharges/history/user/{userId}")
    List<RechargeResponse> getRechargeHistoryByUserId(@PathVariable Long userId);

    @GetMapping("/api/recharges/{rechargeId}")
    RechargeResponse getRechargeByRechargeId(@PathVariable String rechargeId);

    @org.springframework.web.bind.annotation.DeleteMapping("/api/recharges/internal/user/{userId}")
    void deleteAllRechargesForUser(
            @PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestHeader("X-Internal-Secret") String internalSecret);

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    class RechargeResponse {
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
}
