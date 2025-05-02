package com.swann.paymentservice.service;

import com.swann.paymentservice.config.KafkaConfig;
import com.swann.paymentservice.dto.PaymentResponse;
import com.swann.paymentservice.event.PaymentProcessedEvent;
import com.swann.paymentservice.model.Payment;
import com.swann.paymentservice.repository.PaymentRepository;
import com.swann.paymentservice.service.impl.PaymentServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<Object> valueCaptor;

    private UUID paymentId;
    private UUID orderId;
    private UUID customerId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(new BigDecimal("39.98"))
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() {
        // Given
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        // Then
        verify(paymentRepository).findByOrderId(orderId);
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(orderId, response.getOrderId());
        assertEquals(new BigDecimal("39.98"), response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    void getPaymentByOrderId_WhenPaymentNotFound_ShouldThrowException() {
        // Given
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> paymentService.getPaymentByOrderId(orderId));
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    void processPayment_ShouldCreatePaymentAndPublishEvent() {
        // Given
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            savedPayment.setPaymentId(paymentId); // Simulate ID generation
            return savedPayment;
        });

        // When
        PaymentResponse response = paymentService.processPayment(orderId, customerId, new BigDecimal("39.98"));

        // Then
        verify(paymentRepository).findByOrderId(orderId);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(orderId, capturedPayment.getOrderId());
        assertEquals(new BigDecimal("39.98"), capturedPayment.getAmount());
        assertEquals("COMPLETED", capturedPayment.getStatus());

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());
        assertEquals(KafkaConfig.PAYMENT_PROCESSED_TOPIC, topicCaptor.getValue());
        assertEquals(orderId.toString(), keyCaptor.getValue());
        assertTrue(valueCaptor.getValue() instanceof PaymentProcessedEvent);
        PaymentProcessedEvent event = (PaymentProcessedEvent) valueCaptor.getValue();
        assertEquals(paymentId, event.getPaymentId());
        assertEquals(orderId, event.getOrderId());
        assertEquals(new BigDecimal("39.98"), event.getAmount());
        assertEquals("COMPLETED", event.getStatus());

        // Verify response
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(orderId, response.getOrderId());
        assertEquals(new BigDecimal("39.98"), response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    void processPayment_WhenPaymentAlreadyExists_ShouldThrowException() {
        // Given
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(IllegalStateException.class, () -> paymentService.processPayment(orderId, customerId, new BigDecimal("39.98")));
        verify(paymentRepository).findByOrderId(orderId);
        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}