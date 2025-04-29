package com.swann.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private UUID customerId;
    private List<OrderItemRequest> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}