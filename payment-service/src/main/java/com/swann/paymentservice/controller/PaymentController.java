package com.swann.paymentservice.controller;

import com.swann.paymentservice.dto.PaymentResponse;
import com.swann.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments in the e-commerce system")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get payment by order ID", description = "Retrieves payment information for a specific order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found and returned"),
        @ApiResponse(responseCode = "404", description = "Payment not found for the order"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@Parameter(description = "Unique identifier of the order") @PathVariable UUID orderId) {
        log.info("Received request to get payment for order: {}", orderId);
        try {
            PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Payment not found for order: {}", orderId);
            return ResponseEntity.notFound().build();
        }
    }
}