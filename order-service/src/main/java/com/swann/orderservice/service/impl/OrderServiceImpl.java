package com.swann.orderservice.service.impl;

import com.swann.orderservice.config.KafkaConfig;
import com.swann.orderservice.dto.CreateOrderRequest;
import com.swann.orderservice.dto.OrderResponse;
import com.swann.orderservice.event.OrderCreatedEvent;
import com.swann.orderservice.model.Order;
import com.swann.orderservice.model.OrderItem;
import com.swann.orderservice.repository.OrderRepository;
import com.swann.orderservice.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        // Create order entity
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .status("PENDING") // Initial status
                .orderItems(new ArrayList<>())
                .build();
        
        // Create order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> OrderItem.builder()
                        .order(order)
                        .productId(itemRequest.getProductId())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(itemRequest.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
        
        order.setOrderItems(orderItems);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Publish order created event
        publishOrderCreatedEvent(savedOrder);
        
        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        log.info("Getting order by ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
        
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(UUID customerId) {
        log.info("Getting orders for customer: {}", customerId);
        
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    private void publishOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemDto(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .collect(Collectors.toList());
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getOrderId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                itemDtos
        );
        
        log.info("Publishing order created event for order: {}", order.getOrderId());
        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC, order.getOrderId().toString(), event);
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .orderItemId(item.getOrderItemId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}