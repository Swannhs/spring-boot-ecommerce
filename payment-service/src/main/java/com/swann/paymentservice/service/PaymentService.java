package com.swann.paymentservice.service;

import com.swann.paymentservice.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    
    /**
     * Get payment by order ID
     * 
     * @param orderId the order ID
     * @return the payment response
     */
    PaymentResponse getPaymentByOrderId(UUID orderId);
    
    /**
     * Process payment for an order (called by Kafka consumer)
     * 
     * @param orderId the order ID
     * @param amount the payment amount
     * @return the payment response
     */
    PaymentResponse processPayment(UUID orderId, UUID customerId, java.math.BigDecimal amount);
}