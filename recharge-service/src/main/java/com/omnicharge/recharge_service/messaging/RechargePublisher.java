package com.omnicharge.recharge_service.messaging;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RechargePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(String message){

        rabbitTemplate.convertAndSend(
                "rechargeQueue",
                message
        );
    }
}