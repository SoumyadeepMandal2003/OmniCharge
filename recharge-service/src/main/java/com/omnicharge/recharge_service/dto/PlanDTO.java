package com.omnicharge.recharge_service.dto;

import lombok.Data;

@Data
public class PlanDTO {

    private Long id;

    private String description;

    private Double price;

    private OperatorDTO operator;
}