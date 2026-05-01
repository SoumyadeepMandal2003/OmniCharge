package com.omnicharge.user.service;

import com.omnicharge.user.client.PaymentClient;
import com.omnicharge.user.client.PaymentClient.TransactionResponse;
import com.omnicharge.user.client.RechargeClient;
import com.omnicharge.user.client.RechargeClient.RechargeResponse;
import com.omnicharge.user.exception.ResourceNotFoundException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDashboardServiceTest {

    @Mock private RechargeClient rechargeClient;
    @Mock private PaymentClient paymentClient;
    @InjectMocks private UserDashboardService dashboardService;

    @Test
    void getRechargeHistory_success() {
        RechargeResponse r = RechargeResponse.builder()
                .rechargeId("uuid-1").mobileNumber("9876543210")
                .operatorName("Airtel").planName("299 Plan")
                .amount(new BigDecimal("299")).status("SUCCESS").build();
        when(rechargeClient.getRechargeHistoryByUserId(1L)).thenReturn(List.of(r));

        List<RechargeResponse> result = dashboardService.getRechargeHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRechargeId()).isEqualTo("uuid-1");
    }

    @Test
    void getRechargeHistory_serviceDown_returnsEmpty() {
        when(rechargeClient.getRechargeHistoryByUserId(1L))
                .thenThrow(FeignException.NotFound.class);

        List<RechargeResponse> result = dashboardService.getRechargeHistory(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void getRechargeByRechargeId_notFound_throwsException() {
        when(rechargeClient.getRechargeByRechargeId("bad-id"))
                .thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> dashboardService.getRechargeByRechargeId("bad-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    @Test
    void getMyTransactions_success() {
        TransactionResponse t = TransactionResponse.builder()
                .transactionId("txn-1").rechargeId("uuid-1")
                .amount(new BigDecimal("299")).status("SUCCESS").build();
        when(paymentClient.getTransactionsByUserId(1L)).thenReturn(List.of(t));

        List<TransactionResponse> result = dashboardService.getMyTransactions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void getTransactionStatus_success() {
        TransactionResponse t = TransactionResponse.builder()
                .transactionId("txn-1").status("SUCCESS").build();
        when(paymentClient.getTransactionById("txn-1")).thenReturn(t);

        TransactionResponse result = dashboardService.getTransactionStatus("txn-1");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void getTransactionStatus_notFound_throwsException() {
        when(paymentClient.getTransactionById("bad-txn"))
                .thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> dashboardService.getTransactionStatus("bad-txn"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("bad-txn");
    }

    @Test
    void getTransactionByRechargeId_success() {
        TransactionResponse t = TransactionResponse.builder()
                .transactionId("txn-1").rechargeId("uuid-1").status("SUCCESS").build();
        when(paymentClient.getTransactionByRechargeId("uuid-1")).thenReturn(t);

        TransactionResponse result = dashboardService.getTransactionByRechargeId("uuid-1");
        assertThat(result.getTransactionId()).isEqualTo("txn-1");
    }
}
