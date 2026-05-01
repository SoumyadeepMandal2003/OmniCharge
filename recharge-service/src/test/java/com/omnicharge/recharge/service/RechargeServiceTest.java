package com.omnicharge.recharge.service;

import com.omnicharge.recharge.client.OperatorClient;
import com.omnicharge.recharge.client.PaymentClient;
import com.omnicharge.recharge.dto.RechargeDtos.*;
import com.omnicharge.recharge.exception.ResourceNotFoundException;
import com.omnicharge.recharge.model.Recharge;
import com.omnicharge.recharge.repository.RechargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RechargeServiceTest {

    @Mock private RechargeRepository rechargeRepository;
    @Mock private OperatorClient operatorClient;
    @Mock private PaymentClient paymentClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private RechargeService rechargeService;

    private OperatorDto mockOperator;
    private PlanDto mockPlan;

    @BeforeEach
    void setUp() {
        mockOperator = OperatorDto.builder()
                .id(1L)
                .name("Airtel")
                .code("AT")
                .build();

        mockPlan = PlanDto.builder()
                .id(10L)
                .name("299 Plan")
                .price(new BigDecimal("299.00"))
                .validityDays(28)
                .operatorId(1L)
                .operatorName("Airtel")
                .build();
    }

    @Test
    void initiateRecharge_success() {
        RechargeRequest req = new RechargeRequest("9876543210", 1L, 10L);
        when(operatorClient.getOperatorById(1L)).thenReturn(mockOperator);
        when(operatorClient.getPlanById(10L)).thenReturn(mockPlan);

        Recharge saved = Recharge.builder().id(1L).rechargeId("test-uuid")
                .mobileNumber("9876543210").operatorName("Airtel").planName("299 Plan")
                .amount(new BigDecimal("299.00")).validityDays(28)
                .status(Recharge.RechargeStatus.SUCCESS).build();

        when(rechargeRepository.save(any())).thenReturn(saved);
        when(paymentClient.processPayment(any())).thenReturn(
                new PaymentClient.PaymentResponse("txn-123", "SUCCESS", "Payment processed"));

        RechargeResponse result = rechargeService.initiateRecharge(1L, req);

        assertThat(result).isNotNull();
        verify(rabbitTemplate).convertAndSend(eq(RechargeService.EXCHANGE),
                eq(RechargeService.ROUTING_KEY), any(RechargeResponse.class));
    }

    @Test
    void initiateRecharge_planOperatorMismatch_throwsException() {
        RechargeRequest req = new RechargeRequest("9876543210", 1L, 10L);
        PlanDto wrongPlan = PlanDto.builder().id(10L).name("Plan").price(BigDecimal.TEN)
                .validityDays(28).operatorId(2L).build(); // different operator
        when(operatorClient.getOperatorById(1L)).thenReturn(mockOperator);
        when(operatorClient.getPlanById(10L)).thenReturn(wrongPlan);

        assertThatThrownBy(() -> rechargeService.initiateRecharge(1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRechargeHistory_returnsListForUser() {
        Recharge r = Recharge.builder().id(1L).userId(1L).status(Recharge.RechargeStatus.SUCCESS)
                .amount(BigDecimal.TEN).build();
        when(rechargeRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(r));

        List<RechargeResponse> result = rechargeService.getRechargeHistory(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void getRechargeByRechargeId_notFound_throws() {
        when(rechargeRepository.findByRechargeId("bad-id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> rechargeService.getRechargeByRechargeId("bad-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRechargeById_found_returnsRecharge() {
        Recharge r = Recharge.builder().id(100L).rechargeId("RCH-100").status(Recharge.RechargeStatus.SUCCESS).build();
        when(rechargeRepository.findById(100L)).thenReturn(Optional.of(r));
        RechargeResponse response = rechargeService.getRechargeById(100L);
        assertThat(response).isNotNull();
        assertThat(response.getRechargeId()).isEqualTo("RCH-100");
    }

    @Test
    void initiateRecharge_paymentThrowsException_returnsFailed() {
        RechargeRequest req = new RechargeRequest("9876543210", 1L, 10L);
        when(operatorClient.getOperatorById(1L)).thenReturn(mockOperator);
        when(operatorClient.getPlanById(10L)).thenReturn(mockPlan);

        Recharge saved = Recharge.builder().id(1L).rechargeId("RCH-FAIL").status(Recharge.RechargeStatus.PENDING).build();
        when(rechargeRepository.save(any())).thenReturn(saved);
        when(paymentClient.processPayment(any())).thenThrow(new RuntimeException("Payment Gateway Error"));

        RechargeResponse result = rechargeService.initiateRecharge(1L, req);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("FAILED");
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
