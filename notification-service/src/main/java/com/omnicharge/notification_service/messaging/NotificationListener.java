package com.omnicharge.notification_service.messaging;

import org.springframework.amqp.rabbit.annotation.Queue; // 👈 Make sure this is imported!
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    // 👇 Change 'queues' to 'queuesToDeclare' and wrap it in @Queue
    @RabbitListener(queuesToDeclare = @Queue("rechargeQueue"))
    public void receive(String message){

        System.out.println("Notification: " + message);
    }
}