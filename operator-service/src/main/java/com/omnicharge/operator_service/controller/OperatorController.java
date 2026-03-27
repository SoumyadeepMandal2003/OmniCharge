package com.omnicharge.operator_service.controller;

import com.omnicharge.operator_service.entity.Operator;
import com.omnicharge.operator_service.entity.Plan;
import com.omnicharge.operator_service.service.OperatorService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    @PostMapping
    public Operator addOperator(@RequestBody Operator operator) {

        return operatorService.saveOperator(operator);
    }

    @GetMapping
    public List<Operator> getOperators() {

        return operatorService.getAllOperators();
    }

    @PostMapping("/plans")
    public Plan addPlan(@RequestBody Plan plan) {

        return operatorService.savePlan(plan);
    }

    @GetMapping("/plans/{operatorId}")
    public List<Plan> getPlans(@PathVariable Long operatorId) {

        return operatorService.getPlans(operatorId);
    }
}