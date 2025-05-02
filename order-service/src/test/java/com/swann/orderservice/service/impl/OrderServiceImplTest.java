package com.swann.orderservice.service.impl;

import com.swann.orderservice.config.KafkaConfig;
import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;
import com.swann.orderservice.event.OrderCreatedEvent;
import com.swann.orderservice.model.Order;
import com.swann.orderservice.model.OrderItem;
import com.swann.orderservice.repository.OrderRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<Object> valueCaptor;

    private UUID orderId;
    private UUID customerId;
    private UUID productId;
    private Order order;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Setup order
        OrderItem orderItem = OrderItem.builder()
                .orderItemId(UUID.randomUUID())
                .productId(productId)
                .quantity(2)
                .unitPrice(new BigDecimal("19.99"))
                .build();

        order = Order.builder()
                .orderId(orderId)
                .customerId(customerId)
                .totalAmount(new BigDecimal("39.98"))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(new ArrayList<>(Collections.singletonList(orderItem)))
                .build();

        orderItem.setOrder(order);

        // Setup create order request
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                productId,
                2,
                new BigDecimal("19.99")
        );

        createOrderRequest = new CreateOrderRequest(
                customerId,
                Collections.singletonList(itemRequest),
                new BigDecimal("39.98")
        );
    }

    @Test
    void createOrder_ShouldCreateOrderAndPublishEvent() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setOrderId(orderId); // Simulate ID generation
            return savedOrder;
        });

        // When
        OrderResponse response = orderService.createOrder(createOrderRequest);

        // Then
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(customerId, capturedOrder.getCustomerId());
        assertEquals(new BigDecimal("39.98"), capturedOrder.getTotalAmount());
        assertEquals("PENDING", capturedOrder.getStatus());
        assertEquals(1, capturedOrder.getOrderItems().size());
        assertEquals(productId, capturedOrder.getOrderItems().get(0).getProductId());
        assertEquals(2, capturedOrder.getOrderItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), capturedOrder.getOrderItems().get(0).getUnitPrice());

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());
        assertEquals(KafkaConfig.ORDER_CREATED_TOPIC, topicCaptor.getValue());
        assertEquals(orderId.toString(), keyCaptor.getValue());
        assertTrue(valueCaptor.getValue() instanceof OrderCreatedEvent);
        OrderCreatedEvent event = (OrderCreatedEvent) valueCaptor.getValue();
        assertEquals(orderId, event.getOrderId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(new BigDecimal("39.98"), event.getTotalAmount());
        assertEquals("PENDING", event.getStatus());
        assertEquals(1, event.getItems().size());
        assertEquals(productId, event.getItems().get(0).getProductId());
        assertEquals(2, event.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), event.getItems().get(0).getUnitPrice());

        // Verify response
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(new BigDecimal("39.98"), response.getTotalAmount());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(productId, response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), response.getItems().get(0).getUnitPrice());
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        OrderResponse response = orderService.getOrderById(orderId);

        // Then
        verify(orderRepository).findById(orderId);
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(new BigDecimal("39.98"), response.getTotalAmount());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(productId, response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), response.getItems().get(0).getUnitPrice());
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldThrowException() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(orderId));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrdersByCustomerId_ShouldReturnOrders() {
        // Given
        when(orderRepository.findByCustomerId(customerId)).thenReturn(Collections.singletonList(order));

        // When
        List<OrderResponse> responses = orderService.getOrdersByCustomerId(customerId);

        // Then
        verify(orderRepository).findByCustomerId(customerId);
        assertEquals(1, responses.size());
        OrderResponse response = responses.get(0);
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(new BigDecimal("39.98"), response.getTotalAmount());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(productId, response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("19.99"), response.getItems().get(0).getUnitPrice());
    }

    @Test
    void getOrdersByCustomerId_WhenNoOrders_ShouldReturnEmptyList() {
        // Given
        when(orderRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // When
        List<OrderResponse> responses = orderService.getOrdersByCustomerId(customerId);

        // Then
        verify(orderRepository).findByCustomerId(customerId);
        assertTrue(responses.isEmpty());
    }
}