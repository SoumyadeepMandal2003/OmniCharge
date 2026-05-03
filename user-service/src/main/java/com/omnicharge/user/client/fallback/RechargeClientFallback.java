package com.omnicharge.user.client.fallback;

import com.omnicharge.user.client.RechargeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class RechargeClientFallback implements RechargeClient {

    @Override
    public List<RechargeResponse> getRechargeHistoryByUserId(Long userId, String authHeader) {
        log.error("recharge-service unavailable — returning empty history for userId={}", userId);
        return Collections.emptyList();
    }

    @Override
    public RechargeResponse getRechargeByRechargeId(String rechargeId, String authHeader) {
        log.error("recharge-service unavailable — fallback for getRechargeByRechargeId({})", rechargeId);
        throw new RuntimeException("Recharge service is currently unavailable. Please try again later.");
    }
}
