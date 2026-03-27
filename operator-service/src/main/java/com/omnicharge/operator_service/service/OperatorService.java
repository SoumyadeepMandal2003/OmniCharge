package com.omnicharge.operator_service.service;

import com.omnicharge.operator_service.entity.Operator;
import com.omnicharge.operator_service.entity.Plan;
import com.omnicharge.operator_service.repository.OperatorRepository;
import com.omnicharge.operator_service.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorRepository operatorRepository;
    private final PlanRepository planRepository;

    public Operator saveOperator(Operator operator) {

        return operatorRepository.save(operator);
    }

    public List<Operator> getAllOperators() {

        return operatorRepository.findAll();
    }

    public Plan savePlan(Plan plan) {

        return planRepository.save(plan);
    }

    public List<Plan> getPlans(Long operatorId) {

        return planRepository.findByOperatorId(operatorId);
    }
}