package com.swann.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;
import com.swann.orderservice.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;

    private UUID orderId;
    private UUID customerId;
    private OrderResponse orderResponse;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        // Setup order response
        OrderResponse.OrderItemResponse itemResponse = OrderResponse.OrderItemResponse.builder()
                .orderItemId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("19.99"))
                .build();

        orderResponse = OrderResponse.builder()
                .orderId(orderId)
                .customerId(customerId)
                .totalAmount(new BigDecimal("39.98"))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(Collections.singletonList(itemResponse))
                .build();

        // Setup create order request
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                UUID.randomUUID(),
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
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(39.98))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(19.99));
    }

    @Test
    void getOrderById_ShouldReturnOrder() throws Exception {
        when(orderService.getOrderById(orderId)).thenReturn(orderResponse);

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.totalAmount").value(39.98))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrderById(orderId)).thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByCustomerId_ShouldReturnOrders() throws Exception {
        List<OrderResponse> orders = new ArrayList<>();
        orders.add(orderResponse);

        when(orderService.getOrdersByCustomerId(customerId)).thenReturn(orders);

        mockMvc.perform(get("/orders/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
                .andExpect(jsonPath("$[0].totalAmount").value(39.98))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getOrdersByCustomerId_WhenNoOrders_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrdersByCustomerId(customerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/orders/customer/{customerId}", customerId))
                .andExpect(status().isNotFound());
    }
}
