package com.omnicharge.operator.service;

import com.omnicharge.operator.dto.OperatorDtos.*;
import com.omnicharge.operator.exception.ResourceNotFoundException;
import com.omnicharge.operator.model.Operator;
import com.omnicharge.operator.model.Plan;
import com.omnicharge.operator.repository.OperatorRepository;
import com.omnicharge.operator.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorService {

    private final OperatorRepository operatorRepository;
    private final PlanRepository planRepository;

    public List<OperatorResponse> getAllActiveOperators() {
        return operatorRepository.findByActiveTrue().stream().map(this::toOperatorResponse).toList();
    }

    public OperatorResponse getOperatorById(Long id) {
        return operatorRepository.findById(id)
                .map(this::toOperatorResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + id));
    }

    @Transactional
    public OperatorResponse createOperator(OperatorRequest request) {
        if (operatorRepository.existsByName(request.getName()))
            throw new IllegalArgumentException("Operator already exists: " + request.getName());
        Operator op = Operator.builder()
                .name(request.getName()).code(request.getCode())
                .description(request.getDescription()).active(true).build();
        return toOperatorResponse(operatorRepository.save(op));
    }

    @Transactional
    public OperatorResponse updateOperator(Long id, OperatorRequest request) {
        Operator op = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + id));
        op.setName(request.getName());
        op.setCode(request.getCode());
        op.setDescription(request.getDescription());
        return toOperatorResponse(operatorRepository.save(op));
    }

    @Transactional
    public void deactivateOperator(Long id) {
        Operator op = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + id));
        op.setActive(false);
        operatorRepository.save(op);
    }

    public List<PlanResponse> getPlansByOperator(Long operatorId) {
        if (!operatorRepository.existsById(operatorId))
            throw new ResourceNotFoundException("Operator not found: " + operatorId);
        return planRepository.findByOperatorIdAndActiveTrue(operatorId).stream().map(this::toPlanResponse).toList();
    }

    public PlanResponse getPlanById(Long planId) {
        return planRepository.findById(planId)
                .map(this::toPlanResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
    }

    public List<PlanResponse> getAllActivePlans() {
        return planRepository.findByActiveTrue().stream().map(this::toPlanResponse).toList();
    }

    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        Operator operator = operatorRepository.findById(request.getOperatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found: " + request.getOperatorId()));

        Plan.PlanType planType;
        try {
            planType = Plan.PlanType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid plan type '" + request.getType() + "'. Valid values: PREPAID, POSTPAID, DATA, TALKTIME, COMBO");
        }

        Plan plan = Plan.builder()
                .name(request.getName()).price(request.getPrice())
                .validityDays(request.getValidityDays()).data(request.getData())
                .calls(request.getCalls()).sms(request.getSms())
                .description(request.getDescription())
                .type(planType)
                .active(true).operator(operator).build();
        return toPlanResponse(planRepository.save(plan));
    }

    @Transactional
    public void deactivatePlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
        plan.setActive(false);
        planRepository.save(plan);
    }

    private OperatorResponse toOperatorResponse(Operator op) {
        return OperatorResponse.builder()
                .id(op.getId()).name(op.getName()).code(op.getCode())
                .description(op.getDescription()).active(op.isActive()).build();
    }

    private PlanResponse toPlanResponse(Plan p) {
        return PlanResponse.builder()
                .id(p.getId()).name(p.getName()).price(p.getPrice())
                .validityDays(p.getValidityDays()).data(p.getData())
                .calls(p.getCalls()).sms(p.getSms()).description(p.getDescription())
                .type(p.getType().name()).active(p.isActive())
                .operatorId(p.getOperator().getId()).operatorName(p.getOperator().getName()).build();
    }
}
