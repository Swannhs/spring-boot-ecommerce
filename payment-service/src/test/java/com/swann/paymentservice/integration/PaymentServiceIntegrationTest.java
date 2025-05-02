package com.swann.paymentservice.integration;

import com.swann.paymentservice.dto.PaymentResponse;
import com.swann.paymentservice.event.OrderCreatedEvent;
import com.swann.paymentservice.model.Payment;
import com.swann.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(com.swann.paymentservice.TestcontainersConfiguration.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"order-created", "payment-processed"})
@DirtiesContext
@Testcontainers
public class PaymentServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private String baseUrl;
    private UUID orderId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/payments";
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        
        // Clean up the database before each test
        paymentRepository.deleteAll();
    }

    @Test
    void getPaymentByOrderId_WhenPaymentNotFound_ShouldReturnNotFound() {
        // When
        ResponseEntity<PaymentResponse> response = restTemplate.getForEntity(
                baseUrl + "/order/" + UUID.randomUUID(), PaymentResponse.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void processPayment_WhenOrderCreatedEventReceived_ShouldCreatePayment() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setTotalAmount(new BigDecimal("39.98"));
        event.setStatus("PENDING");
        event.setItems(new ArrayList<>());

        // When
        kafkaTemplate.send("order-created", orderId.toString(), event);

        // Then
        // Wait for the payment to be processed
        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            List<Payment> payments = paymentRepository.findAll();
            return !payments.isEmpty();
        });

        // Verify the payment was created
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        assertNotNull(payment);
        assertEquals(orderId, payment.getOrderId());
        assertEquals(new BigDecimal("39.98"), payment.getAmount());
        assertEquals("COMPLETED", payment.getStatus());

        // Verify the payment can be retrieved via API
        ResponseEntity<PaymentResponse> response = restTemplate.getForEntity(
                baseUrl + "/order/" + orderId, PaymentResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        PaymentResponse paymentResponse = response.getBody();
        assertEquals(payment.getPaymentId(), paymentResponse.getPaymentId());
        assertEquals(orderId, paymentResponse.getOrderId());
        assertEquals(new BigDecimal("39.98"), paymentResponse.getAmount());
        assertEquals("COMPLETED", paymentResponse.getStatus());
    }
}