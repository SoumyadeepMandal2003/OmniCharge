package com.omnicharge.notification.service;

import com.omnicharge.notification.dto.RechargeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendRechargeConfirmation_success_doesNotThrow() {
        RechargeEvent event = RechargeEvent.builder()
                .rechargeId("test-id").mobileNumber("9876543210")
                .operatorName("Airtel").planName("299 Plan")
                .amount(new BigDecimal("299.00")).validityDays(28)
                .transactionId("txn-123").status("SUCCESS").build();

        assertThatCode(() -> notificationService.sendRechargeConfirmation(event))
                .doesNotThrowAnyException();
    }

    @Test
    void sendRechargeConfirmation_failed_doesNotThrow() {
        RechargeEvent event = RechargeEvent.builder()
                .rechargeId("test-id").mobileNumber("9876543210")
                .amount(new BigDecimal("299.00")).status("FAILED").build();

        assertThatCode(() -> notificationService.sendRechargeConfirmation(event))
                .doesNotThrowAnyException();
    }
}
