package com.omnicharge.operator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.operator.dto.OperatorDtos.*;
import com.omnicharge.operator.service.OperatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperatorController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OperatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperatorService operatorService;

    @Autowired
    private ObjectMapper objectMapper;

    private OperatorResponse operatorResponse;
    private PlanResponse planResponse;

    @BeforeEach
    void setUp() {
        operatorResponse = OperatorResponse.builder()
                .id(1L)
                .name("Airtel")
                .code("AT")
                .description("Bharti Airtel")
                .active(true)
                .build();

        planResponse = PlanResponse.builder()
                .id(1L)
                .name("Unlimited 299")
                .price(new BigDecimal("299.00"))
                .validityDays(28)
                .data("1.5GB/day")
                .calls("Unlimited")
                .sms("100/day")
                .description("Popular 28 days plan")
                .type("PREPAID")
                .active(true)
                .operatorId(1L)
                .operatorName("Airtel")
                .build();
    }

    @Test
    void getAllOperators_ReturnsOk() throws Exception {
        when(operatorService.getAllActiveOperators()).thenReturn(List.of(operatorResponse));

        mockMvc.perform(get("/api/operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Airtel"));
    }

    @Test
    void getOperatorById_ReturnsOk() throws Exception {
        when(operatorService.getOperatorById(1L)).thenReturn(operatorResponse);

        mockMvc.perform(get("/api/operators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Airtel"));
    }

    @Test
    void createOperator_ReturnsCreated() throws Exception {
        OperatorRequest request = new OperatorRequest();
        request.setName("Airtel");
        request.setCode("AT");
        request.setDescription("Bharti Airtel");

        when(operatorService.createOperator(any(OperatorRequest.class))).thenReturn(operatorResponse);

        mockMvc.perform(post("/api/admin/operators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Airtel"));
    }

    @Test
    void updateOperator_ReturnsOk() throws Exception {
        OperatorRequest request = new OperatorRequest();
        request.setName("Airtel");
        request.setCode("AT");
        request.setDescription("Bharti Airtel");

        when(operatorService.updateOperator(anyLong(), any(OperatorRequest.class))).thenReturn(operatorResponse);

        mockMvc.perform(put("/api/admin/operators/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Airtel"));
    }

    @Test
    void deactivateOperator_ReturnsNoContent() throws Exception {
        doNothing().when(operatorService).deactivateOperator(1L);

        mockMvc.perform(delete("/api/admin/operators/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getPlansByOperator_ReturnsOk() throws Exception {
        when(operatorService.getPlansByOperator(1L)).thenReturn(List.of(planResponse));

        mockMvc.perform(get("/api/operators/1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Unlimited 299"));
    }

    @Test
    void getAllPlans_ReturnsOk() throws Exception {
        when(operatorService.getAllActivePlans()).thenReturn(List.of(planResponse));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Unlimited 299"));
    }

    @Test
    void getPlanById_ReturnsOk() throws Exception {
        when(operatorService.getPlanById(1L)).thenReturn(planResponse);

        mockMvc.perform(get("/api/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Unlimited 299"));
    }

    @Test
    void createPlan_ReturnsCreated() throws Exception {
        PlanRequest request = new PlanRequest();
        request.setName("Unlimited 299");
        request.setPrice(new BigDecimal("299.00"));
        request.setValidityDays(28);
        request.setOperatorId(1L);
        request.setType("PREPAID");

        when(operatorService.createPlan(any(PlanRequest.class))).thenReturn(planResponse);

        mockMvc.perform(post("/api/admin/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Unlimited 299"));
    }

    @Test
    void deactivatePlan_ReturnsNoContent() throws Exception {
        doNothing().when(operatorService).deactivatePlan(1L);

        mockMvc.perform(delete("/api/admin/plans/1"))
                .andExpect(status().isNoContent());
    }
}
