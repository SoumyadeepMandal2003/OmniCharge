package com.omnicharge.user.client.fallback;

import com.omnicharge.user.client.PaymentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class PaymentClientFallback implements PaymentClient {

    @Override
    public List<TransactionResponse> getTransactionsByUserId(Long userId, String authHeader) {
        log.error("payment-service unavailable — returning empty transactions for userId={}", userId);
        return Collections.emptyList();
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId, String authHeader) {
        log.error("payment-service unavailable — fallback for getTransactionById({})", transactionId);
        throw new RuntimeException("Payment service is currently unavailable. Please try again later.");
    }

    @Override
    public TransactionResponse getTransactionByRechargeId(String rechargeId, String authHeader) {
        log.error("payment-service unavailable — fallback for getTransactionByRechargeId({})", rechargeId);
        throw new RuntimeException("Payment service is currently unavailable. Please try again later.");
    }
}
