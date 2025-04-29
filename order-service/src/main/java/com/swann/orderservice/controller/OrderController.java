package com.swann.orderservice.controller;

import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;
import com.swann.orderservice.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
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
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
     * 
     * @param request the order creation request
     * @return the created order response with 201 Created status
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Received request to create order for customer: {}", request.getCustomerId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get an order by ID
     * 
     * @param orderId the order ID
     * @return the order response with 200 OK status, or 404 Not Found if order doesn't exist
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        log.info("Received request to get order with ID: {}", orderId);
        try {
            OrderResponse response = orderService.getOrderById(orderId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Order not found with ID: {}", orderId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all orders for a customer
     * 
     * @param customerId the customer ID
     * @return list of order responses with 200 OK status, or 404 Not Found if no orders exist
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable UUID customerId) {
        log.info("Received request to get orders for customer: {}", customerId);
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        
        if (orders.isEmpty()) {
            log.warn("No orders found for customer: {}", customerId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orders);
    }
}