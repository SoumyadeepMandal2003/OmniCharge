package com.omnicharge.recharge_service.service;

import com.omnicharge.recharge_service.client.OperatorClient;
import com.omnicharge.recharge_service.client.PaymentClient;
import com.omnicharge.recharge_service.dto.PlanDTO;
import com.omnicharge.recharge_service.dto.PaymentDTO;
import com.omnicharge.recharge_service.entity.Recharge;
import com.omnicharge.recharge_service.messaging.RechargePublisher;
import com.omnicharge.recharge_service.repository.RechargeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RechargeService {

    private final RechargeRepository rechargeRepository;

    private final OperatorClient operatorClient;

    private final PaymentClient paymentClient;

    private final RechargePublisher publisher;

    public Recharge createRecharge(Recharge recharge){

        // validate plan from operator-service
        List<PlanDTO> plans =
                operatorClient.getPlans(recharge.getOperatorId());

        PlanDTO selectedPlan =
                plans.stream()

                        .filter(p -> p.getId()
                                .equals(recharge.getPlanId()))

                        .findFirst()

                        .orElseThrow(() ->
                                new RuntimeException("Invalid plan"));

        recharge.setAmount(selectedPlan.getPrice());

        recharge.setCreatedAt(LocalDateTime.now());

        recharge.setStatus("PENDING");

        Recharge savedRecharge =
                rechargeRepository.save(recharge);

        // call payment-service
        PaymentDTO paymentRequest = new PaymentDTO();

        paymentRequest.setRechargeId(savedRecharge.getId());

        paymentRequest.setAmount(savedRecharge.getAmount());

        PaymentDTO paymentResponse =
                paymentClient.processPayment(paymentRequest);

        savedRecharge.setStatus(paymentResponse.getStatus());

        return rechargeRepository.save(savedRecharge);
    }

    public List<Recharge> getHistory(String mobile){

        return rechargeRepository.findByMobileNumber(mobile);
    }
}