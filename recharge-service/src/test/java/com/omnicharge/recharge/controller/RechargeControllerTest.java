package com.omnicharge.recharge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.recharge.dto.RechargeDtos.*;
import com.omnicharge.recharge.security.JwtUtil;
import com.omnicharge.recharge.service.RechargeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = RechargeController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class)
)
@AutoConfigureMockMvc(addFilters = false)
class RechargeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private RechargeService rechargeService;
    @MockBean private JwtUtil jwtUtil;

    private RechargeResponse sampleRecharge;

    @BeforeEach
    void setUp() {
        sampleRecharge = RechargeResponse.builder()
                .id(1L).rechargeId("RCH-001").mobileNumber("9876543210")
                .operatorName("Airtel").planName("299 Plan")
                .amount(new BigDecimal("299.00")).validityDays(28)
                .status("SUCCESS").transactionId("TXN-001").build();
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void initiateRecharge_validRequest_returnsCreated() throws Exception {
        RechargeRequest req = new RechargeRequest("9876543210", 1L, 1L);
        when(jwtUtil.extractUserId(any())).thenReturn(1L);
        when(rechargeService.initiateRecharge(eq(1L), any())).thenReturn(sampleRecharge);

        mockMvc.perform(post("/api/recharges")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rechargeId").value("RCH-001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getHistory_returnsOk() throws Exception {
        when(jwtUtil.extractUserId(any())).thenReturn(1L);
        when(rechargeService.getRechargeHistory(1L)).thenReturn(List.of(sampleRecharge));

        mockMvc.perform(get("/api/recharges/history")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getByRechargeId_returnsOk() throws Exception {
        when(rechargeService.getRechargeByRechargeId("RCH-001")).thenReturn(sampleRecharge);

        mockMvc.perform(get("/api/recharges/RCH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rechargeId").value("RCH-001"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void initiateRecharge_invalidMobile_returnsBadRequest() throws Exception {
        RechargeRequest req = new RechargeRequest("12345", 1L, 1L); // invalid mobile
        when(jwtUtil.extractUserId(any())).thenReturn(1L);

        mockMvc.perform(post("/api/recharges")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
