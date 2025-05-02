package com.swann.paymentservice.controller;

import com.swann.paymentservice.dto.PaymentResponse;
import com.swann.paymentservice.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private UUID paymentId;
    private UUID orderId;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        
        paymentResponse = PaymentResponse.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(new BigDecimal("39.98"))
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() throws Exception {
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(paymentResponse);

        mockMvc.perform(get("/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.amount").value(39.98))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getPaymentByOrderId_WhenPaymentNotFound_ShouldReturnNotFound() throws Exception {
        when(paymentService.getPaymentByOrderId(orderId)).thenThrow(new EntityNotFoundException("Payment not found"));

        mockMvc.perform(get("/payments/order/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }
}