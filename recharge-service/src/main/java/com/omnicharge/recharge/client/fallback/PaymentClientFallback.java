package com.omnicharge.recharge.client.fallback;

import com.omnicharge.recharge.client.PaymentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentClientFallback implements PaymentClient {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.error("payment-service is unavailable — fallback triggered for processPayment(rechargeId={})",
                request.getRechargeId());
        throw new RuntimeException("Payment service is currently unavailable. Please try again later.");
    }
}
