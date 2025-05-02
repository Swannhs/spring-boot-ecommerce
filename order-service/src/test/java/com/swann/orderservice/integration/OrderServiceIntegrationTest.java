package com.swann.orderservice.integration;

import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;
import com.swann.orderservice.model.Order;
import com.swann.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.swann.orderservice.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"order-created"})
@DirtiesContext
@Testcontainers
public class OrderServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private String baseUrl;
    private UUID customerId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/orders";
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Clean up the database before each test
        orderRepository.deleteAll();
    }

    @Test
    void createOrder_ShouldCreateOrderAndReturnResponse() {
        // Given
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                productId,
                2,
                new BigDecimal("19.99")
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                Collections.singletonList(itemRequest),
                new BigDecimal("39.98")
        );

        // When
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(baseUrl, request, OrderResponse.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        OrderResponse orderResponse = response.getBody();
        assertNotNull(orderResponse.getOrderId());
        assertEquals(customerId, orderResponse.getCustomerId());
        assertEquals(new BigDecimal("39.98"), orderResponse.getTotalAmount());
        assertEquals("PENDING", orderResponse.getStatus());
        assertEquals(1, orderResponse.getItems().size());
        assertEquals(productId, orderResponse.getItems().get(0).getProductId());
        assertEquals(2, orderResponse.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), orderResponse.getItems().get(0).getUnitPrice());

        // Verify the order was saved to the database
        List<Order> orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        Order savedOrder = orders.get(0);
        assertEquals(customerId, savedOrder.getCustomerId());
        assertEquals(new BigDecimal("39.98"), savedOrder.getTotalAmount());
        assertEquals("PENDING", savedOrder.getStatus());
        assertEquals(1, savedOrder.getOrderItems().size());
        assertEquals(productId, savedOrder.getOrderItems().get(0).getProductId());
        assertEquals(2, savedOrder.getOrderItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), savedOrder.getOrderItems().get(0).getUnitPrice());
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Given
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                productId,
                2,
                new BigDecimal("19.99")
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                Collections.singletonList(itemRequest),
                new BigDecimal("39.98")
        );

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(baseUrl, request, OrderResponse.class);
        UUID orderId = createResponse.getBody().getOrderId();

        // When
        ResponseEntity<OrderResponse> response = restTemplate.getForEntity(baseUrl + "/" + orderId, OrderResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        OrderResponse orderResponse = response.getBody();
        assertEquals(orderId, orderResponse.getOrderId());
        assertEquals(customerId, orderResponse.getCustomerId());
        assertEquals(new BigDecimal("39.98"), orderResponse.getTotalAmount());
        assertEquals("PENDING", orderResponse.getStatus());
        assertEquals(1, orderResponse.getItems().size());
        assertEquals(productId, orderResponse.getItems().get(0).getProductId());
        assertEquals(2, orderResponse.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), orderResponse.getItems().get(0).getUnitPrice());
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldReturnNotFound() {
        // When
        ResponseEntity<OrderResponse> response = restTemplate.getForEntity(baseUrl + "/" + UUID.randomUUID(), OrderResponse.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getOrdersByCustomerId_ShouldReturnOrders() {
        // Given
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                productId,
                2,
                new BigDecimal("19.99")
        );

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                Collections.singletonList(itemRequest),
                new BigDecimal("39.98")
        );

        restTemplate.postForEntity(baseUrl, request, OrderResponse.class);

        // When
        ResponseEntity<OrderResponse[]> response = restTemplate.getForEntity(
                baseUrl + "/customer/" + customerId, OrderResponse[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);

        OrderResponse orderResponse = response.getBody()[0];
        assertEquals(customerId, orderResponse.getCustomerId());
        assertEquals(new BigDecimal("39.98"), orderResponse.getTotalAmount());
        assertEquals("PENDING", orderResponse.getStatus());
        assertEquals(1, orderResponse.getItems().size());
        assertEquals(productId, orderResponse.getItems().get(0).getProductId());
        assertEquals(2, orderResponse.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), orderResponse.getItems().get(0).getUnitPrice());
    }

    @Test
    void getOrdersByCustomerId_WhenNoOrders_ShouldReturnNotFound() {
        // When
        ResponseEntity<OrderResponse[]> response = restTemplate.getForEntity(
                baseUrl + "/customer/" + UUID.randomUUID(), OrderResponse[].class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
