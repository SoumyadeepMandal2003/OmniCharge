package com.omnicharge.operator.controller;

import com.omnicharge.operator.dto.OperatorDtos.*;
import com.omnicharge.operator.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Operator API", description = "Telecom operators and recharge plans")
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping("/operators")
    @Operation(summary = "Get all active operators")
    public ResponseEntity<List<OperatorResponse>> getAllOperators() {
        return ResponseEntity.ok(operatorService.getAllActiveOperators());
    }

    @GetMapping("/operators/{id}")
    @Operation(summary = "Get operator by ID")
    public ResponseEntity<OperatorResponse> getOperatorById(@PathVariable Long id) {
        return ResponseEntity.ok(operatorService.getOperatorById(id));
    }

    @PostMapping("/admin/operators")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create operator (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperatorResponse> createOperator(@Valid @RequestBody OperatorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.createOperator(request));
    }

    @PutMapping("/admin/operators/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update operator (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperatorResponse> updateOperator(@PathVariable Long id,
                                                            @Valid @RequestBody OperatorRequest request) {
        return ResponseEntity.ok(operatorService.updateOperator(id, request));
    }

    @DeleteMapping("/admin/operators/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate operator (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deactivateOperator(@PathVariable Long id) {
        operatorService.deactivateOperator(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/operators/{operatorId}/plans")
    @Operation(summary = "Get plans by operator")
    public ResponseEntity<List<PlanResponse>> getPlansByOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(operatorService.getPlansByOperator(operatorId));
    }

    @GetMapping("/plans")
    @Operation(summary = "Get all active plans")
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(operatorService.getAllActivePlans());
    }

    @GetMapping("/plans/{planId}")
    @Operation(summary = "Get plan by ID")
    public ResponseEntity<PlanResponse> getPlanById(@PathVariable Long planId) {
        return ResponseEntity.ok(operatorService.getPlanById(planId));
    }

    @PostMapping("/admin/plans")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create plan (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.createPlan(request));
    }

    @DeleteMapping("/admin/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate plan (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long planId) {
        operatorService.deactivatePlan(planId);
        return ResponseEntity.noContent().build();
    }
}
