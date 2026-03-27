package com.omnicharge.payment_service.service;

import com.omnicharge.payment_service.entity.Payment;
import com.omnicharge.payment_service.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment processPayment(Payment payment){

        payment.setStatus("SUCCESS");

        payment.setTransactionId(
                UUID.randomUUID().toString()
        );

        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
}