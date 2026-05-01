package com.omnicharge.user.service;

import com.omnicharge.user.client.PaymentClient;
import com.omnicharge.user.client.PaymentClient.TransactionResponse;
import com.omnicharge.user.client.RechargeClient;
import com.omnicharge.user.client.RechargeClient.RechargeResponse;
import com.omnicharge.user.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDashboardService {

    private final RechargeClient rechargeClient;
    private final PaymentClient paymentClient;

    /**
     * Returns the full recharge history for the logged-in user.
     * Delegates to recharge-service via Feign.
     */
    public List<RechargeResponse> getRechargeHistory(Long userId) {
        try {
            return rechargeClient.getRechargeHistoryByUserId(userId);
        } catch (FeignException.NotFound e) {
            return List.of();
        } catch (FeignException e) {
            log.error("Failed to fetch recharge history for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Unable to fetch recharge history. Please try again later.");
        }
    }

    /**
     * Returns a single recharge by its rechargeId (UUID).
     */
    public RechargeResponse getRechargeByRechargeId(String rechargeId) {
        try {
            return rechargeClient.getRechargeByRechargeId(rechargeId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Recharge not found: " + rechargeId);
        } catch (FeignException e) {
            log.error("Failed to fetch recharge {}: {}", rechargeId, e.getMessage());
            throw new RuntimeException("Unable to fetch recharge details. Please try again later.");
        }
    }

    /**
     * Returns all transactions for the logged-in user.
     * Delegates to payment-service via Feign.
     */
    public List<TransactionResponse> getMyTransactions(Long userId) {
        try {
            return paymentClient.getTransactionsByUserId(userId);
        } catch (FeignException.NotFound e) {
            return List.of();
        } catch (FeignException e) {
            log.error("Failed to fetch transactions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Unable to fetch transactions. Please try again later.");
        }
    }

    /**
     * Returns the transaction status by transactionId.
     */
    public TransactionResponse getTransactionStatus(String transactionId) {
        try {
            return paymentClient.getTransactionById(transactionId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        } catch (FeignException e) {
            log.error("Failed to fetch transaction {}: {}", transactionId, e.getMessage());
            throw new RuntimeException("Unable to fetch transaction status. Please try again later.");
        }
    }

    /**
     * Returns the transaction linked to a specific rechargeId.
     */
    public TransactionResponse getTransactionByRechargeId(String rechargeId) {
        try {
            return paymentClient.getTransactionByRechargeId(rechargeId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("No transaction found for recharge: " + rechargeId);
        } catch (FeignException e) {
            log.error("Failed to fetch transaction for recharge {}: {}", rechargeId, e.getMessage());
            throw new RuntimeException("Unable to fetch transaction. Please try again later.");
        }
    }
}
