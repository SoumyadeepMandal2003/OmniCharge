package com.omnicharge.notification.messaging;

import com.omnicharge.notification.dto.RechargeEvent;
import com.omnicharge.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RechargeEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RechargeEventListener listener;

    private RechargeEvent event;

    @BeforeEach
    void setUp() {
        event = new RechargeEvent();
        event.setRechargeId("RCH123");
        event.setStatus("SUCCESS");
    }

    @Test
    void handleRechargeCompleted_Success_CallsNotificationService() {
        doNothing().when(notificationService).sendRechargeConfirmation(any(RechargeEvent.class));
        
        listener.handleRechargeCompleted(event);
        
        verify(notificationService, times(1)).sendRechargeConfirmation(any(RechargeEvent.class));
    }

    @Test
    void handleRechargeCompleted_WithFailedStatus_CallsNotificationService() {
        event.setStatus("FAILED");
        doNothing().when(notificationService).sendRechargeConfirmation(any(RechargeEvent.class));
        
        listener.handleRechargeCompleted(event);
        
        verify(notificationService, times(1)).sendRechargeConfirmation(any(RechargeEvent.class));
    }

    @Test
    void handleRechargeCompleted_NullEvent_ThrowsNPE() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            listener.handleRechargeCompleted(null);
        });
        verify(notificationService, never()).sendRechargeConfirmation(any());
    }

    @Test
    void handleRechargeCompleted_ServiceThrowsException_CatchBlockExecuted() {
        doThrow(new RuntimeException("Simulated exception")).when(notificationService).sendRechargeConfirmation(any(RechargeEvent.class));
        
        listener.handleRechargeCompleted(event);
        
        verify(notificationService, times(1)).sendRechargeConfirmation(any(RechargeEvent.class));
        // Should not bubble up exception
    }
    
    @Test
    void handleRechargeCompleted_MultipleCalls_ServiceCalledMultipleTimes() {
        doNothing().when(notificationService).sendRechargeConfirmation(any(RechargeEvent.class));
        
        listener.handleRechargeCompleted(event);
        listener.handleRechargeCompleted(event);
        
        verify(notificationService, times(2)).sendRechargeConfirmation(any(RechargeEvent.class));
    }
}
