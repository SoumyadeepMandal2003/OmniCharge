package com.omnicharge.user.controller;

import com.omnicharge.user.client.PaymentClient.TransactionResponse;
import com.omnicharge.user.client.RechargeClient.RechargeResponse;
import com.omnicharge.user.dto.UserDtos.*;
import com.omnicharge.user.service.UserDashboardService;
import com.omnicharge.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "User API", description = "Profile management, recharge history, transaction status")
public class UserController {

    private final UserService userService;
    private final UserDashboardService dashboardService;

    // ── Internal endpoint called by auth-service via Feign ──────────────────

    @PostMapping("/api/users/internal/create")
    @Operation(summary = "Internal — create user profile (called by auth-service only)")
    public ResponseEntity<UserResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createProfile(request));
    }

    // ── Profile ─────────────────────────────────────────────────────────────

    @GetMapping("/api/users/me")
    @Operation(summary = "Get my profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserByEmail(authentication.getName()));
    }

    @PutMapping("/api/users/me")
    @Operation(summary = "Update my profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
                                                       @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse current = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(userService.updateProfile(current.getId(), request));
    }

    @GetMapping("/api/users/{id}")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── Recharge History ────────────────────────────────────────────────────

    @GetMapping("/api/users/me/recharges")
    @Operation(summary = "View my recharge history", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<RechargeResponse>> getMyRechargeHistory(Authentication authentication) {
        UserResponse current = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(dashboardService.getRechargeHistory(current.getId()));
    }

    @GetMapping("/api/users/me/recharges/{rechargeId}")
    @Operation(summary = "Get a specific recharge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RechargeResponse> getMyRecharge(@PathVariable String rechargeId) {
        return ResponseEntity.ok(dashboardService.getRechargeByRechargeId(rechargeId));
    }

    // ── Transaction Status ──────────────────────────────────────────────────

    @GetMapping("/api/users/me/transactions")
    @Operation(summary = "View my transaction history", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(Authentication authentication) {
        UserResponse current = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(dashboardService.getMyTransactions(current.getId()));
    }

    @GetMapping("/api/users/me/transactions/{transactionId}")
    @Operation(summary = "View transaction status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TransactionResponse> getTransactionStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(dashboardService.getTransactionStatus(transactionId));
    }

    @GetMapping("/api/users/me/recharges/{rechargeId}/transaction")
    @Operation(summary = "Get payment transaction for a recharge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TransactionResponse> getTransactionByRecharge(@PathVariable String rechargeId) {
        return ResponseEntity.ok(dashboardService.getTransactionByRechargeId(rechargeId));
    }
}
