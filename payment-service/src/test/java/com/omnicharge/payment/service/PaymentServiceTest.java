package com.omnicharge.payment.service;

import com.omnicharge.payment.dto.PaymentDtos.*;
import com.omnicharge.payment.exception.ResourceNotFoundException;
import com.omnicharge.payment.model.Transaction;
import com.omnicharge.payment.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private PaymentService paymentService;

    @Test
    void processPayment_success() {
        PaymentRequest req = new PaymentRequest("recharge-123", 1L, new BigDecimal("299.00"), "Test recharge");
        when(transactionRepository.findByRechargeId("recharge-123")).thenReturn(Optional.empty());

        Transaction saved = Transaction.builder()
                .transactionId("txn-uuid").rechargeId("recharge-123")
                .userId(1L).amount(new BigDecimal("299.00"))
                .status(Transaction.TransactionStatus.SUCCESS).build();
        when(transactionRepository.save(any())).thenReturn(saved);

        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTransactionId()).isNotNull();
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void processPayment_duplicate_throwsException() {
        PaymentRequest req = new PaymentRequest("recharge-123", 1L, new BigDecimal("299.00"), "Test");
        Transaction existing = Transaction.builder().transactionId("existing-txn").build();
        when(transactionRepository.findByRechargeId("recharge-123")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paymentService.processPayment(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already processed");
    }

    @Test
    void processPayment_exceedsLimit_fails() {
        PaymentRequest req = new PaymentRequest("recharge-xyz", 1L, new BigDecimal("15000.00"), "Large amount");
        when(transactionRepository.findByRechargeId("recharge-xyz")).thenReturn(Optional.empty());

        Transaction pending = Transaction.builder().transactionId("txn-abc")
                .status(Transaction.TransactionStatus.PENDING).build();
        when(transactionRepository.save(any())).thenReturn(pending);

        assertThatThrownBy(() -> paymentService.processPayment(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment processing failed");
    }

    @Test
    void getTransactionById_notFound_throws() {
        when(transactionRepository.findByTransactionId("bad-id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentService.getTransactionById("bad-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTransactionsByUser_returnsList() {
        Transaction t = Transaction.builder().id(1L).userId(1L)
                .amount(new BigDecimal("299")).status(Transaction.TransactionStatus.SUCCESS).build();
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(t));

        List<TransactionResponse> result = paymentService.getTransactionsByUser(1L);
        assertThat(result).hasSize(1);
    }
}
