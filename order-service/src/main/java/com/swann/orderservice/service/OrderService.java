package com.swann.orderservice.service;

import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    
    /**
     * Create a new order
     * 
     * @param request the order creation request
     * @return the created order response
     */
    OrderResponse createOrder(CreateOrderRequest request);
    
    /**
     * Get an order by ID
     * 
     * @param orderId the order ID
     * @return the order response
     */
    OrderResponse getOrderById(UUID orderId);
    
    /**
     * Get all orders for a customer
     * 
     * @param customerId the customer ID
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByCustomerId(UUID customerId);
}