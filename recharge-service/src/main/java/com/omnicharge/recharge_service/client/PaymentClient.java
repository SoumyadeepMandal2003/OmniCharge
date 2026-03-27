package com.omnicharge.recharge_service.client;

import com.omnicharge.recharge_service.dto.PaymentDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/payment")
    PaymentDTO processPayment(@RequestBody PaymentDTO paymentDTO);
}