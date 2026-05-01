package com.omnicharge.notification.messaging;

import com.omnicharge.notification.dto.RechargeEvent;
import com.omnicharge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RechargeEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue:recharge.completed.queue}")
    public void handleRechargeCompleted(RechargeEvent event) {
        log.info("Received recharge event: rechargeId={}, status={}", event.getRechargeId(), event.getStatus());
        try {
            notificationService.sendRechargeConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to send notification for recharge {}: {}", event.getRechargeId(), e.getMessage());
        }
    }
}
