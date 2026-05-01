package com.omnicharge.recharge.service;

import com.omnicharge.recharge.client.OperatorClient;
import com.omnicharge.recharge.client.PaymentClient;
import com.omnicharge.recharge.dto.RechargeDtos.*;
import com.omnicharge.recharge.exception.ResourceNotFoundException;
import com.omnicharge.recharge.model.Recharge;
import com.omnicharge.recharge.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechargeService {

    private final RechargeRepository rechargeRepository;
    private final OperatorClient operatorClient;
    private final PaymentClient paymentClient;
    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE = "omnicharge.exchange";
    public static final String ROUTING_KEY = "recharge.completed";

    @Transactional
    public RechargeResponse initiateRecharge(Long userId, RechargeRequest request) {
        // Fetch operator and plan from operator-service
        OperatorDto operator = operatorClient.getOperatorById(request.getOperatorId());
        PlanDto plan = operatorClient.getPlanById(request.getPlanId());

        if (!plan.getOperatorId().equals(request.getOperatorId())) {
            throw new IllegalArgumentException("Plan does not belong to the specified operator");
        }

        String rechargeId = UUID.randomUUID().toString();

        Recharge recharge = Recharge.builder()
                .rechargeId(rechargeId)
                .userId(userId)
                .mobileNumber(request.getMobileNumber())
                .operatorId(operator.getId())
                .operatorName(operator.getName())
                .planId(plan.getId())
                .planName(plan.getName())
                .amount(plan.getPrice())
                .validityDays(plan.getValidityDays())
                .status(Recharge.RechargeStatus.PENDING)
                .build();

        recharge = rechargeRepository.save(recharge);

        // Process payment via payment-service
        try {
            recharge.setStatus(Recharge.RechargeStatus.PROCESSING);
            rechargeRepository.save(recharge);

            PaymentClient.PaymentResponse payment = paymentClient.processPayment(
                    PaymentClient.PaymentRequest.builder()
                            .rechargeId(rechargeId)
                            .userId(userId)
                            .amount(plan.getPrice())
                            .description("Recharge for " + request.getMobileNumber() + " - " + plan.getName())
                            .build()
            );

            recharge.setTransactionId(payment.getTransactionId());
            recharge.setStatus(Recharge.RechargeStatus.SUCCESS);
            recharge.setCompletedAt(LocalDateTime.now());
            rechargeRepository.save(recharge);

            // Publish async event to RabbitMQ — non-blocking, failure does not affect recharge outcome
            publishRechargeEvent(recharge);
            log.info("Recharge {} completed successfully", rechargeId);

        } catch (Exception e) {
            recharge.setStatus(Recharge.RechargeStatus.FAILED);
            recharge.setFailureReason(e.getMessage());
            rechargeRepository.save(recharge);
            log.error("Recharge {} failed: {}", rechargeId, e.getMessage());
        }

        return toResponse(recharge);
    }

    public List<RechargeResponse> getRechargeHistory(Long userId) {
        return rechargeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RechargeResponse getRechargeByRechargeId(String rechargeId) {
        return rechargeRepository.findByRechargeId(rechargeId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Recharge not found: " + rechargeId));
    }

    public RechargeResponse getRechargeById(Long id) {
        return rechargeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Recharge not found: " + id));
    }

    private RechargeResponse toResponse(Recharge r) {
        return RechargeResponse.builder()
                .id(r.getId()).rechargeId(r.getRechargeId())
                .mobileNumber(r.getMobileNumber()).operatorName(r.getOperatorName())
                .planName(r.getPlanName()).amount(r.getAmount())
                .validityDays(r.getValidityDays()).status(r.getStatus().name())
                .transactionId(r.getTransactionId()).createdAt(r.getCreatedAt())
                .completedAt(r.getCompletedAt()).build();
    }

    /**
     * Publishes a recharge-completed event to RabbitMQ.
     * Failure here is non-fatal — the recharge is already SUCCESS in the DB.
     * The notification will simply not be sent, which is acceptable.
     */
    private void publishRechargeEvent(Recharge recharge) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, toResponse(recharge));
            log.info("Published recharge event for rechargeId={}", recharge.getRechargeId());
        } catch (Exception e) {
            log.error("Failed to publish recharge event for rechargeId={}: {}. " +
                    "Recharge is SUCCESS but notification may not be sent.",
                    recharge.getRechargeId(), e.getMessage());
        }
    }
}
