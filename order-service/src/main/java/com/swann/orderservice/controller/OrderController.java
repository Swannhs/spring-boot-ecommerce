package com.swann.orderservice.controller;

import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;
import com.swann.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders in the e-commerce system")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order for a customer with the specified items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to create order for customer: {}", request.getCustomerId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found and returned"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@Parameter(description = "Unique identifier of the order") @PathVariable UUID orderId) {
        log.info("Received request to get order with ID: {}", orderId);
        try {
            OrderResponse response = orderService.getOrderById(orderId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Order not found with ID: {}", orderId);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get orders by customer ID", description = "Retrieves all orders for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders found and returned"),
        @ApiResponse(responseCode = "404", description = "No orders found for the customer"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@Parameter(description = "Unique identifier of the customer") @PathVariable UUID customerId) {
        log.info("Received request to get orders for customer: {}", customerId);
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        
        if (orders.isEmpty()) {
            log.warn("No orders found for customer: {}", customerId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orders);
    }
}