package com.swann.paymentservice.controller;

import com.swann.paymentservice.dto.PaymentResponse;
import com.swann.paymentservice.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Get payment by order ID
     * 
     * @param orderId the order ID
     * @return the payment response with 200 OK status, or 404 Not Found if payment doesn't exist
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable UUID orderId) {
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