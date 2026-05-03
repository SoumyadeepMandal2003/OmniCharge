package com.omnicharge.payment.service;

import com.omnicharge.payment.dto.PaymentDtos.*;
import com.omnicharge.payment.exception.ResourceNotFoundException;
import com.omnicharge.payment.model.Transaction;
import com.omnicharge.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;

    @Value("${payment.max-amount:10000}")
    private BigDecimal maxTransactionAmount;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Check for duplicate recharge payment
        if (transactionRepository.findByRechargeId(request.getRechargeId()).isPresent()) {
            throw new IllegalArgumentException("Payment already processed for recharge: " + request.getRechargeId());
        }

        String txnId = UUID.randomUUID().toString();

        Transaction txn = Transaction.builder()
                .transactionId(txnId)
                .rechargeId(request.getRechargeId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod("WALLET")
                .build();

        transactionRepository.save(txn);

        try {
            // Simulate payment gateway processing to check for transaction
            simulatePaymentGateway(request.getAmount());

            txn.setStatus(Transaction.TransactionStatus.SUCCESS);
            txn.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            log.info("Payment {} processed successfully for recharge {}", txnId, request.getRechargeId());
            return PaymentResponse.builder()
                    .transactionId(txnId)
                    .status("SUCCESS")
                    .message("Payment processed successfully")
                    .build();

        } catch (Exception e) {
            txn.setStatus(Transaction.TransactionStatus.FAILED);
            txn.setFailureReason(e.getMessage());
            transactionRepository.save(txn);
            log.error("Payment {} failed: {}", txnId, e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    public TransactionResponse getTransactionById(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    public List<TransactionResponse> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public TransactionResponse getTransactionByRechargeId(String rechargeId) {
        return transactionRepository.findByRechargeId(rechargeId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for recharge: " + rechargeId));
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void simulatePaymentGateway(java.math.BigDecimal amount) {
        // Simulate occasional gateway failures for realistic testing
        if (amount.compareTo(maxTransactionAmount) > 0) {
            throw new RuntimeException("Amount exceeds maximum transaction limit of " + maxTransactionAmount);
        }
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).transactionId(t.getTransactionId())
                .rechargeId(t.getRechargeId()).userId(t.getUserId())
                .amount(t.getAmount()).description(t.getDescription())
                .status(t.getStatus().name()).paymentMethod(t.getPaymentMethod())
                .createdAt(t.getCreatedAt()).processedAt(t.getProcessedAt())
                .build();
    }
}
