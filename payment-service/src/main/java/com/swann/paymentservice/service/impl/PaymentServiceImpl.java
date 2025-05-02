package com.swann.paymentservice.service.impl;

import com.swann.paymentservice.config.KafkaConfig;
import com.swann.paymentservice.dto.PaymentResponse;
import com.swann.paymentservice.event.PaymentProcessedEvent;
import com.swann.paymentservice.model.Payment;
import com.swann.paymentservice.repository.PaymentRepository;
import com.swann.paymentservice.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        log.info("Getting payment for order: {}", orderId);
        
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order ID: " + orderId));
        
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(UUID orderId, UUID customerId, BigDecimal amount) {
        log.info("Processing payment for order: {}", orderId);
        
        // Check if payment already exists
        paymentRepository.findByOrderId(orderId).ifPresent(existingPayment -> {
            log.warn("Payment already exists for order: {}", orderId);
            throw new IllegalStateException("Payment already processed for order: " + orderId);
        });
        
        // Create new payment
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status("COMPLETED") // Simplified for demo purposes
                .build();
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        // Publish payment processed event
        publishPaymentProcessedEvent(savedPayment);
        
        return mapToPaymentResponse(savedPayment);
    }
    
    private void publishPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus()
        );
        
        log.info("Publishing payment processed event for payment: {}", payment.getPaymentId());
        kafkaTemplate.send(KafkaConfig.PAYMENT_PROCESSED_TOPIC, payment.getOrderId().toString(), event);
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}