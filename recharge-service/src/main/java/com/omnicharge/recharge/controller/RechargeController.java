package com.omnicharge.recharge.controller;

import com.omnicharge.recharge.dto.RechargeDtos.*;
import com.omnicharge.recharge.security.JwtUtil;
import com.omnicharge.recharge.service.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recharges")
@RequiredArgsConstructor
@Tag(name = "Recharge API", description = "Mobile recharge operations")
public class RechargeController {

    private final RechargeService rechargeService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Initiate a recharge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RechargeResponse> initiateRecharge(
            @Valid @RequestBody RechargeRequest request,
            HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rechargeService.initiateRecharge(userId, request));
    }

    @GetMapping("/history")
    @Operation(summary = "Get my recharge history", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<RechargeResponse>> getHistory(HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(rechargeService.getRechargeHistory(userId));
    }

    @GetMapping("/history/user/{userId}")
    @Operation(summary = "Get recharge history by userId (internal use)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<RechargeResponse>> getHistoryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(rechargeService.getRechargeHistory(userId));
    }

    @GetMapping("/{rechargeId}")
    @Operation(summary = "Get recharge by recharge ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RechargeResponse> getByRechargeId(@PathVariable String rechargeId) {
        return ResponseEntity.ok(rechargeService.getRechargeByRechargeId(rechargeId));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get recharge by DB id", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RechargeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rechargeService.getRechargeById(id));
    }

    @DeleteMapping("/internal/user/{userId}")
    @Operation(summary = "Internal — delete all recharges for a user (called by auth-service only)")
    public ResponseEntity<Void> deleteAllRechargesForUser(
            @PathVariable Long userId,
            @RequestHeader("X-Internal-Secret") String internalSecret) {
        rechargeService.deleteAllRechargesForUser(userId, internalSecret);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalStateException("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
