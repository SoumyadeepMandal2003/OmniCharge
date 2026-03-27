package com.omnicharge.recharge_service.service;

import com.omnicharge.recharge_service.client.OperatorClient;
import com.omnicharge.recharge_service.client.PaymentClient;
import com.omnicharge.recharge_service.dto.OperatorDTO;
import com.omnicharge.recharge_service.dto.PaymentDTO;
import com.omnicharge.recharge_service.dto.PlanDTO;
import com.omnicharge.recharge_service.entity.Recharge;
import com.omnicharge.recharge_service.repository.RechargeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RechargeServiceTest {

    @Mock
    private RechargeRepository rechargeRepository;

    @Mock
    private OperatorClient operatorClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private RechargeService rechargeService;

    @Test
    void testCreateRecharge_success(){

        Recharge inputRecharge = new Recharge();

        inputRecharge.setMobileNumber("9876543210");
        inputRecharge.setOperatorId(1L);
        inputRecharge.setPlanId(1L);

        OperatorDTO operatorDTO = new OperatorDTO();
        operatorDTO.setId(1L);
        operatorDTO.setName("Jio");

        PlanDTO planDTO = new PlanDTO();
        planDTO.setId(1L);
        planDTO.setPrice(299.0);
        planDTO.setOperator(operatorDTO);

        PaymentDTO paymentResponse = new PaymentDTO();
        paymentResponse.setStatus("SUCCESS");

        when(operatorClient.getPlans(1L))
                .thenReturn(List.of(planDTO));

        when(rechargeRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentClient.processPayment(any()))
                .thenReturn(paymentResponse);

        Recharge result =
                rechargeService.createRecharge(inputRecharge);

        assertEquals("SUCCESS", result.getStatus());

        assertEquals(299.0, result.getAmount());
    }

}