package com.omnicharge.recharge_service.controller;

import com.omnicharge.recharge_service.entity.Recharge;
import com.omnicharge.recharge_service.service.RechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final RechargeService rechargeService;

    @PostMapping
    public Recharge recharge(@RequestBody Recharge recharge){

        return rechargeService.createRecharge(recharge);
    }

    @GetMapping("/history/{mobile}")
    public List<Recharge> history(@PathVariable String mobile){

        return rechargeService.getHistory(mobile);
    }
}