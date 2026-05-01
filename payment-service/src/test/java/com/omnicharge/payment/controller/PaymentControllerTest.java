package com.omnicharge.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.payment.dto.PaymentDtos.*;
import com.omnicharge.payment.service.PaymentService;
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
    controllers = PaymentController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableWebSecurity.class)
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private PaymentService paymentService;

    private TransactionResponse sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = TransactionResponse.builder()
                .id(1L).transactionId("TXN-001").rechargeId("RCH-001")
                .userId(1L).amount(new BigDecimal("299.00"))
                .status("SUCCESS").paymentMethod("WALLET").build();
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void processPayment_validRequest_returnsOk() throws Exception {
        PaymentRequest req = new PaymentRequest("RCH-001", 1L, new BigDecimal("299.00"), "Test recharge");
        PaymentResponse response = PaymentResponse.builder()
                .transactionId("TXN-001").status("SUCCESS").message("Payment processed").build();
        when(paymentService.processPayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/payments/process")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("TXN-001"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getTransaction_returnsOk() throws Exception {
        when(paymentService.getTransactionById("TXN-001")).thenReturn(sampleTransaction);

        mockMvc.perform(get("/api/payments/transaction/TXN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getUserTransactions_returnsOk() throws Exception {
        when(paymentService.getTransactionsByUser(1L)).thenReturn(List.of(sampleTransaction));

        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllTransactions_adminRole_returnsOk() throws Exception {
        when(paymentService.getAllTransactions()).thenReturn(List.of(sampleTransaction));

        mockMvc.perform(get("/api/payments/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void processPayment_missingRechargeId_returnsBadRequest() throws Exception {
        PaymentRequest req = new PaymentRequest(null, 1L, new BigDecimal("299.00"), "Test");

        mockMvc.perform(post("/api/payments/process")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
