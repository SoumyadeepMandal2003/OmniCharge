package com.omnicharge.notification;

import com.omnicharge.notification.dto.RechargeEvent;
import com.omnicharge.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    private RechargeEvent successEvent;
    private RechargeEvent failedEvent;

    @BeforeEach
    void setUp() {
        successEvent = new RechargeEvent();
        successEvent.setRechargeId("RCH123");
        successEvent.setTransactionId("TXN123");
        successEvent.setMobileNumber("9876543210");
        successEvent.setOperatorName("Airtel");
        successEvent.setPlanName("Unlimited");
        successEvent.setAmount(java.math.BigDecimal.valueOf(299.0));
        successEvent.setValidityDays(28);
        successEvent.setStatus("SUCCESS");
        successEvent.setCreatedAt(LocalDateTime.now());

        failedEvent = new RechargeEvent();
        failedEvent.setRechargeId("RCH456");
        failedEvent.setMobileNumber("9876543210");
        failedEvent.setAmount(java.math.BigDecimal.valueOf(100.0));
        failedEvent.setStatus("FAILED");
    }

    @Test
    void sendRechargeConfirmation_StatusSuccess_ExecutesWithoutError() {
        assertDoesNotThrow(() -> notificationService.sendRechargeConfirmation(successEvent));
    }

    @Test
    void sendRechargeConfirmation_StatusFailed_ExecutesWithoutError() {
        assertDoesNotThrow(() -> notificationService.sendRechargeConfirmation(failedEvent));
    }

    @Test
    void sendRechargeConfirmation_StatusPending_DoesNothing() {
        RechargeEvent pendingEvent = new RechargeEvent();
        pendingEvent.setStatus("PENDING");
        assertDoesNotThrow(() -> notificationService.sendRechargeConfirmation(pendingEvent));
    }

    @Test
    void sendRechargeConfirmation_StatusNull_DoesNothing() {
        RechargeEvent nullStatusEvent = new RechargeEvent();
        assertDoesNotThrow(() -> notificationService.sendRechargeConfirmation(nullStatusEvent));
    }

    @Test
    void sendRechargeConfirmation_MissingFieldsOnSuccess_HandlesGracefully() {
        RechargeEvent incompleteEvent = new RechargeEvent();
        incompleteEvent.setStatus("SUCCESS");
        // amount, mobileNumber are null, so String.format may print "null" but shouldn't throw exception
        assertDoesNotThrow(() -> notificationService.sendRechargeConfirmation(incompleteEvent));
    }
}
