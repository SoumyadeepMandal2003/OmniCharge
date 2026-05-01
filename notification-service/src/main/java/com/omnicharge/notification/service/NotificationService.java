package com.omnicharge.notification.service;

import com.omnicharge.notification.dto.RechargeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    /**
     * Sends a recharge confirmation notification.
     * In production this would integrate with an SMS gateway (Twilio/AWS SNS)
     * or email service (SendGrid/SES). Here we log for demonstration.
     */

    public void sendRechargeConfirmation(RechargeEvent event) {
        if ("SUCCESS".equals(event.getStatus())) {
            String smsMessage = buildSmsMessage(event);
            String emailSubject = "Recharge Successful - OmniCharge";
            String emailBody = buildEmailBody(event);

            // SMS notification (simulated)
            sendSms(event.getMobileNumber(), smsMessage);

            // Email notification (simulated - would use user's email from user-service)
            log.info("EMAIL to user {} | Subject: {} | Body preview: {}",
                    event.getRechargeId(), emailSubject, emailBody.substring(0, Math.min(80, emailBody.length())));

        } else if ("FAILED".equals(event.getStatus())) {
            String failMsg = String.format("Dear Customer, your recharge of Rs.%.2f for %s has FAILED. " +
                    "Recharge ID: %s. Please try again. - OmniCharge",
                    event.getAmount(), event.getMobileNumber(), event.getRechargeId());
            sendSms(event.getMobileNumber(), failMsg);
        }
    }

    private void sendSms(String mobile, String message) {
        // Pluggable: replace with Twilio / AWS SNS / MSG91 SDK call
        log.info("SMS -> [{}]: {}", mobile, message);
    }

    private String buildSmsMessage(RechargeEvent event) {
        return String.format(
            "Dear Customer, Rs.%.2f recharge for %s (%s - %s, %d days validity) is successful. " +
            "Txn ID: %s. - OmniCharge",
            event.getAmount(), event.getMobileNumber(),
            event.getOperatorName(), event.getPlanName(),
            event.getValidityDays(), event.getTransactionId());
    }

    private String buildEmailBody(RechargeEvent event) {
        return String.format("""
            Dear Customer,
            
            Your mobile recharge was successful!
            
            Details:
            - Mobile Number : %s
            - Operator      : %s
            - Plan          : %s
            - Amount        : Rs. %.2f
            - Validity      : %d days
            - Transaction ID: %s
            - Recharge ID   : %s
            - Date          : %s
            
            Thank you for using OmniCharge.
            """,
            event.getMobileNumber(), event.getOperatorName(), event.getPlanName(),
            event.getAmount(), event.getValidityDays(), event.getTransactionId(),
            event.getRechargeId(), event.getCreatedAt());
    }
}
