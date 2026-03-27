package com.omnicharge.recharge_service.dto;

import lombok.Data;

@Data
public class PaymentDTO {

    private Long rechargeId;

    private Double amount;

    private String status;

    private String transactionId;
}