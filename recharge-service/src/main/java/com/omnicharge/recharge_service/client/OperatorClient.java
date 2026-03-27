package com.omnicharge.recharge_service.client;

import com.omnicharge.recharge_service.dto.PlanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "OPERATOR-SERVICE")
public interface OperatorClient {

    @GetMapping("/operators/plans/{operatorId}")
    List<PlanDTO> getPlans(@PathVariable Long operatorId);
}