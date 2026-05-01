package com.omnicharge.recharge.client.fallback;

import com.omnicharge.recharge.client.OperatorClient;
import com.omnicharge.recharge.dto.RechargeDtos.OperatorDto;
import com.omnicharge.recharge.dto.RechargeDtos.PlanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OperatorClientFallback implements OperatorClient {

    @Override
    public OperatorDto getOperatorById(Long id) {
        log.error("operator-service is unavailable — fallback triggered for getOperatorById({})", id);
        throw new RuntimeException("Operator service is currently unavailable. Please try again later.");
    }

    @Override
    public PlanDto getPlanById(Long planId) {
        log.error("operator-service is unavailable — fallback triggered for getPlanById({})", planId);
        throw new RuntimeException("Operator service is currently unavailable. Please try again later.");
    }
}
