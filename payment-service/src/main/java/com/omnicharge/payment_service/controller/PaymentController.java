package com.omnicharge.payment_service.controller;

import com.omnicharge.payment_service.entity.Payment;
import com.omnicharge.payment_service.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Payment pay(@RequestBody Payment payment){

        return paymentService.processPayment(payment);
    }
}